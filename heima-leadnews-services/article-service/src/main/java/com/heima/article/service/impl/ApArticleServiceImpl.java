package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApAuthorMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.exception.CustException;
import com.heima.feigns.AdminFeign;
import com.heima.feigns.WemediaFeign;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.dtos.ArticleHomeDTO;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.ArticleConstants;
import com.heima.model.constants.wemedia.WemediaConstants;
import com.heima.model.wemedia.pojos.WmNews;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Autowired
    private WemediaFeign wemediaFeign;
    @Autowired
    private GeneratePageServiceImpl generatePageService;
    @GlobalTransactional(rollbackFor = Exception.class,timeoutMills = 300000)
    @Override
    public void publishArticle(Integer newsId) {
        if(newsId==null){
            return;
        }
        //查询校验文章
        WmNews wmNews = getWmNews(newsId);
        //封装ApArticle对象
        ApArticle apArticle = getApArticle(wmNews);
        //保存文章
        saveOrUpdateArticle(apArticle);
        //保存关联配置和文章内容
        saveConfigAndContent(wmNews, apArticle);
        //页面静态化
        generatePageService.generateArticlePage(wmNews.getContent(),apArticle);
        //更新wmNews
        updateWmNews(newsId, wmNews, apArticle);
        // CustException.cust(AppHttpCodeEnum.SUCCESS); 测试全局事务
        //TODO 通知es索引库添加文章索引 
    }

    /**
     * app端加载文章
     * @param loadtype
     * @param dto loadtype 0：加载更多   1：加载最新
     * @return
     */
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Value("${file.oss.web-site}")
    private String webSite;
    @Value("${file.minio.readPath}")
    private String readPath;
    @Override
    public ResponseResult load(Short loadtype, ArticleHomeDTO dto) {
        Integer size = dto.getSize();
        if(size==null||size<=0){
            size=10;
        }
        dto.setSize(size);
        if(StringUtils.isBlank(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        if(dto.getMaxBehotTime()==null){
            dto.setMaxBehotTime(new Date());
        }
        if(dto.getMinBehotTime()==null){
            dto.setMinBehotTime(new Date());
        }
        //根据loadtype判断加载类型
        if(!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_MORE)
                &&!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            loadtype=ArticleConstants.LOADTYPE_LOAD_MORE;
        }
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto,loadtype);
        for(ApArticle apArticle:apArticles){
            praseArticle(apArticle);
        }

        ResponseResult responseResult = ResponseResult.okResult(apArticles);
        responseResult.setHost(webSite);

        return responseResult;
    }
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public ResponseResult load2(Short loadtypeLoadMore, ArticleHomeDTO dto, boolean firstPage) {
        if(firstPage){

            String articleListJson = (String) redisTemplate
                    .opsForValue().get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if(StringUtils.isNotBlank(articleListJson)){
                List<ApArticle>apArticleList = JSON.parseArray(articleListJson, ApArticle.class);
                for(ApArticle apArticle:apArticleList){
                    praseArticle(apArticle);

                }
                ResponseResult responseResult = ResponseResult.okResult(apArticleList);
                responseResult.setHost(webSite);
                return responseResult;
            }
        }
        return load(loadtypeLoadMore,dto);
    }

    private void praseArticle(ApArticle apArticle) {
        String images = apArticle.getImages();
        if(StringUtils.isNotBlank(images)){
            images = Arrays.stream(images.split(","))
                    .map(url ->webSite+url)
                    .collect(Collectors.joining(","));
            apArticle.setImages(images);
        }
        apArticle.setStaticUrl(readPath+apArticle.getStaticUrl());

    }

    /**
     * 更新wmNews
     * @param newsId
     * @param wmNews
     * @param apArticle
     */
    private void updateWmNews(Integer newsId, WmNews wmNews, ApArticle apArticle) {
        wmNews.setArticleId(apArticle.getId());
        wmNews.setStatus(WemediaConstants.WM_NEWS_PUBLISH_STATUS);
        ResponseResult responseResult = wemediaFeign.updateWmNews(wmNews);
        if(responseResult.getCode()!=AppHttpCodeEnum.SUCCESS.getCode()){
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,responseResult.getErrorMessage());
        }
    }


    /**
     * 保存关联配置和文章内容
     * @param wmNews 自媒体文章信息 apArticle 文章信息
     * @return
     */
    private void saveConfigAndContent(WmNews wmNews, ApArticle apArticle) {
        //保存关联配置
        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        apArticleConfig.setIsComment(true);
        apArticleConfig.setIsForward(true);
        apArticleConfig.setIsDown(false);
        apArticleConfig.setIsDelete(false);
        apArticleConfigMapper.insert(apArticleConfig);

        //保存文章内容
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        apArticleContent.setContent(wmNews.getContent());
        apArticleContentMapper.insert(apArticleContent);

    }

    /**
     * 保存和更新文章
     * @param apArticle
     * @return
     */
    @Autowired
    ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    ApArticleContentMapper apArticleContentMapper;
    private void saveOrUpdateArticle(ApArticle apArticle){
        if(apArticle.getId()==null){//新文章
            apArticle.setCollection(0); // 收藏数
            apArticle.setLikes(0);// 点赞数
            apArticle.setComment(0);// 评论数
            apArticle.setViews(0); // 阅读数
            save(apArticle);
        }else{//更新文章
            //删除关联信息
            ApArticle oldArticle = getById(apArticle.getId());
            if(oldArticle==null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
            }
            updateById(apArticle);
            apArticleConfigMapper.delete(new LambdaQueryWrapper<ApArticleConfig>().eq(ApArticleConfig::getArticleId,apArticle.getId()));
            apArticleContentMapper.delete(new LambdaQueryWrapper<ApArticleContent>().eq(ApArticleContent::getArticleId,apArticle.getId()));
        }
    }

    /**
     * 封装ApArticle对象
     * @param wmNews
     * @return
     */
    @Autowired
    private AdminFeign adminFeign;
    @Autowired
    private ApAuthorMapper apAuthorMapper;
    private ApArticle getApArticle(WmNews wmNews) {
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(wmNews,apArticle);
        apArticle.setId(wmNews.getArticleId());
        apArticle.setFlag((byte)0); // 普通文章
        apArticle.setPublishTime(wmNews.getPublishTime());
        apArticle.setLayout(wmNews.getType());
        ResponseResult<AdChannel> channelResult = adminFeign.findOne(wmNews.getChannelId());
        if(!channelResult.checkCode()){
            log.error("查询频道失败，id:{}"+wmNews.getChannelId());
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用频道失败");
        }
        AdChannel channel = channelResult.getData();
        if(channel==null){
            log.error("查询频道为空，id:{}"+wmNews.getChannelId());
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"频道不存在");
        }
        apArticle.setChannelName(channel.getName());
        //设置作者
        ApAuthor apAuthor = apAuthorMapper.selectOne(new LambdaQueryWrapper<ApAuthor>().eq(ApAuthor::getWmUserId, wmNews.getUserId()));
        if(apAuthor==null){
            log.error("查询作者为空，id:{}"+wmNews.getUserId());
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"作者不存在");
        }
        apArticle.setAuthorId(apAuthor.getId().longValue());
        apArticle.setAuthorName(apAuthor.getName());
        return apArticle;
    }

    /**
     * 查询校验文章
     * @param newsId
     * @return
     */
    private WmNews getWmNews(Integer newsId) {
        ResponseResult result = wemediaFeign.findWmNewsById(newsId);
        if(!result.checkCode()){
            log.error("查询自媒体文章失败，id:{}"+newsId);
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用自媒体文章失败");
        }
        WmNews wmNews = (WmNews) result.getData();
        if(wmNews==null){
            log.error("查询自媒体文章为空，id:{}"+newsId);
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"自媒体文章不存在");
        }
        short status = wmNews.getStatus().shortValue();
        if(status!= WmNews.Status.SUCCESS.getCode()
                &&status!=WemediaConstants.WM_NEWS_AUTH_PASS){
            log.error("查询自媒体文章状态不是已发布，id:{}"+newsId);
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"自媒体文章状态错误");
        }
        return wmNews;
    }
}