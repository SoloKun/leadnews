package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.exception.CustException;
import com.heima.feigns.AdminFeign;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.ArticleConstants;
import com.heima.model.mess.app.AggBehaviorDTO;
import com.heima.utils.common.DateUtils;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.channels.Channel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: HotArticleServiceImpl
 * Package: com.heima.article.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/3 17:21
 * @Version 1.0
 */
@Service

public class HotArticleServiceImpl implements HotArticleService {
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Override
    /**
     * Description: 计算热文章
     */
    public void computeHotArticle() {
        String date = LocalDateTime.now().minusDays(10)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
        List<ApArticle> apArticleList = apArticleMapper.selectArticleByDate(date);
        List<HotArticleVo> hotArticleVoList = computeArticleScore(apArticleList);
        cacheToRedis(hotArticleVoList);
    }

    @Override
    public void updateApArticle(AggBehaviorDTO aggBehavior) {
        ApArticle apArticle = apArticleMapper.selectById(aggBehavior.getArticleId());
        if(apArticle==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        if(aggBehavior.getCollect()!=0){
            int view = (int)(apArticle.getViews()==null?aggBehavior.getView():apArticle.getViews()+aggBehavior.getView());
            apArticle.setViews(view);
        }
        if(aggBehavior.getLike()!=0){
            int like = (int)(apArticle.getLikes()==null?aggBehavior.getLike():apArticle.getLikes()+aggBehavior.getLike());
            apArticle.setLikes(like);
        }
        if(aggBehavior.getComment()!=0){
            int comment = (int)(apArticle.getComment()==null?aggBehavior.getComment():apArticle.getComment()+aggBehavior.getComment());
            apArticle.setComment(comment);
        }
        if(aggBehavior.getView()!=0){
            int view = (int)(apArticle.getViews()==null?aggBehavior.getView():apArticle.getViews()+aggBehavior.getView());
            apArticle.setViews(view);
        }
        apArticleMapper.updateById(apArticle);
        Integer score = computeScore(apArticle);
        String publishStr = DateUtils.dateToString(apArticle.getPublishTime());
        String nowStr = DateUtils.dateToString(new Date());
        if(publishStr.equals(nowStr)) {
            score*=3;
        }
        updateArticleCache(apArticle, score,
                ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId());
        updateArticleCache(apArticle, score,  ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ ArticleConstants.DEFAULT_TAG);

    }

    private void updateArticleCache(ApArticle apArticle, Integer score, String key) {
        boolean flag = false;
        String hotArticleJson = redisTemplate.opsForValue().get(key);

        if(StringUtils.isNotBlank(hotArticleJson)){
            List<HotArticleVo> hotArticleList =
                    JSON.parseArray(hotArticleJson, HotArticleVo.class);
            for(HotArticleVo hotArticleVo:hotArticleList){
                if(hotArticleVo.getId().equals(apArticle.getId())){
                    hotArticleVo.setScore(score);
                    flag = true;
                    break;
                }
            }
            if(!flag){
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle,hotArticleVo);
                hotArticleVo.setScore(score);
                hotArticleList.add(hotArticleVo);
            }
            hotArticleList = hotArticleList.stream()
                    .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                    .limit(30)
                    .collect(Collectors.toList());
        }

    }

    private void cacheToRedis(List<HotArticleVo> hotArticleVoList) {
        ResponseResult<List<AdChannel>>result = adminFeign.selectChannels();
        if(!result.checkCode()){
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用adminFeign.selectChannels()失败");
        }
        List<AdChannel> adChannelList = result.getData();
        for(AdChannel adChannel:adChannelList){
            List<HotArticleVo> hotArticleVos = hotArticleVoList.stream()
                    .filter(hotArticleVo ->
                            hotArticleVo.getChannelId().equals(adChannel.getId())).
                    collect(Collectors.toList());
            sortAndCache(hotArticleVos,ArticleConstants.HOT_ARTICLE_FIRST_PAGE+adChannel.getId());

        }
        sortAndCache(hotArticleVoList,ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ArticleConstants.DEFAULT_TAG);
    }

    private void sortAndCache(List<HotArticleVo> hotArticleVos, String key) {
            hotArticleVos = hotArticleVos.stream()
                    .sorted(Comparator.comparing(HotArticleVo::getScore)
                            .reversed())
                    .limit(30)
                    .collect(Collectors.toList());

            redisTemplate.opsForValue().set(key, JSON.toJSONString(hotArticleVos));

    }

    @Autowired
    AdminFeign adminFeign;
    @Autowired
    // RedisTemplate redisTemplate;
    private StringRedisTemplate redisTemplate;
    private List<HotArticleVo> computeArticleScore(List<ApArticle> apArticleList) {
        return apArticleList.stream().map(apArticle -> {
            HotArticleVo hotArticleVo = new HotArticleVo();
            BeanUtils.copyProperties(apArticle,hotArticleVo);
            Integer score = computeScore(apArticle);
            hotArticleVo.setScore(score);
            return  hotArticleVo;
        }).collect(Collectors.toList());

    }
    private Integer computeScore(ApArticle apArticle){
        Integer score = 0;
        if(apArticle.getViews()!=null){
            score+=apArticle.getViews()* ArticleConstants.HOT_ARTICLE_VIEW_WEIGHT;
        }
        if(apArticle.getLikes()!=null){
            score+=apArticle.getLikes()*ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if(apArticle.getComment()!=null){
            score+=apArticle.getComment()*ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if(apArticle.getCollection()!=null){
            score+=apArticle.getCollection()*ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }
}
