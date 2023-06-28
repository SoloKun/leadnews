package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.aliyun.scan.GreenImageScan;
import com.heima.aliyun.scan.GreenTextScan;
import com.heima.common.exception.CustException;
import com.heima.feigns.AdminFeign;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.message.PublishArticleConstants;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Update;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




/**
 * ClassName: WmNewsAutoScanServiceImpl
 * Package: com.heima.wemedia.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 16:49
 * @Version 1.0
 */
@Service
@Slf4j
public class WmNewsAutoScanServiceImpl  implements WmNewsAutoScanService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Value("${file.oss.web-site}")
    String webSite;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public void autoScanByMediaNews(Integer id) {
        log.info("WmNewsAutoScanServiceImpl autoScanByMediaNews id:{}",id);
        if(id == null){

            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章id不能为空");
        }
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //判断文章状态是否为待审核
        if(!(WmNews.Status.SUBMIT.getCode()==wmNews.getStatus().shortValue())){
            log.info("文章状态不是待审核");
            return;
        }
        //抽取文章文本内容和图片链接
        Map<String,Object> contents = handleTextAndImages(wmNews);
        //DFA 敏感词过滤
        boolean isContentSensitive = handleSensitive(contents.get("content").toString(),wmNews);
        if(isContentSensitive){
            log.info("文章内容包含敏感词");
            return;
        }
        //云审核 文本
        boolean isTextSensitive = handleText(contents.get("content").toString(),wmNews);
        if(isTextSensitive){
            log.info("文章内容包含敏感词");
            return;
        }
        //云审核 图片
        boolean isImageSensitive = handleImages((List<String>) contents.get("images"),wmNews);
        if (isImageSensitive){
            log.info("文章图片包含敏感内容");
            return;
        }
        //修改文章状态为待发布
        UpdateWmNews(wmNews,WmNews.Status.SUCCESS.getCode(),"文章审核通过");
        //TODO 根据文章发布时间，设置定时发布任务
        //延迟时间
        long delay = wmNews.getPublishTime().getTime() - System.currentTimeMillis();
        rabbitTemplate.convertAndSend(PublishArticleConstants.DELAY_DIRECT_EXCHANGE
                ,PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY
                ,wmNews.getId()
                ,message -> {
                    message.getMessageProperties().setHeader(
                            "x-delay",delay<=0?0:delay
                    );
                    return message;
                });
        log.info("文章审核通过，发送延迟消息，id:{}",wmNews.getId());

    }
    @Autowired
    private GreenImageScan greenImageScan;
    private boolean handleImages(List<String> images, WmNews wmNews) {
        boolean isSensitive = false;
        if(CollectionUtils.isEmpty(images)){
            return isSensitive;
        }
        try{
            Map resultMap = greenImageScan.imageUrlScan(images);
            log.info("文章图片审核结果：{}", JSON.toJSONString(resultMap));
            String suggestion = (String) resultMap.get("suggestion");
            switch (suggestion){
                case "review":
                    isSensitive = true;
                    UpdateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "文章图片需要人工审核");
                    break;
                case "block":
                    isSensitive = true;
                    UpdateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "文章图片包含违禁内容："+resultMap.get("reason"));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            isSensitive = true;
            log.error("文章图片云审核失败：{}",e.getMessage());
            UpdateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "文章图片云审核失败");
        }
        return isSensitive;
    }

    @Autowired
    private GreenTextScan greenTextScan;
    private boolean handleText(String content, WmNews wmNews) {
        boolean isSensitive = false;
        try{
            Map ResultMap = greenTextScan.greenTextScan(content);
            String suggestion = (String) ResultMap.get("suggestion");
            log.info("文章内容审核结果：{}", JSON.toJSONString(ResultMap));
            switch (suggestion){
                case "review":
                    isSensitive = true;
                    UpdateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "文章内容需要人工审核");
                    break;
                case "block":
                    isSensitive = true;
                    UpdateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "文章内容包含违禁内容："+ResultMap.get("reason"));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("文章内容云审核失败：{}",e.getMessage());
            UpdateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "文章内容云审核失败");
            isSensitive = true;
        }
        return isSensitive;
    }

    @Autowired
    private AdminFeign adminFeign;
    private boolean handleSensitive(String content, WmNews wmNews) {
        boolean isSensitive = false;
        ResponseResult<List<String>> allSensitiveWords = adminFeign.selectAllSensitives();
        if(allSensitiveWords.getCode().intValue()!=0){
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"调用敏感词服务失败");
        }
        if(allSensitiveWords.getData() == null || allSensitiveWords.getData().size() == 0){
            return isSensitive;
        }
        List<String> allSensitives = allSensitiveWords.getData();
        //判断文章内容是否包含敏感词
        SensitiveWordUtil.initMap(allSensitives);
        Map<String,Integer>resultMap = SensitiveWordUtil.matchWords(content);
        if(resultMap != null && !CollectionUtils.isEmpty(resultMap)) {
            isSensitive = true;
            //修改文章状态为自媒体审核不通过
            UpdateWmNews(wmNews,WmNews.Status.FAIL.getCode(),"包含敏感词："+resultMap.keySet().toString());
        }

        return isSensitive;

    }

    /**
     * 修改文章状态
     * @param wmNews
     * @param status
     * */
    private void UpdateWmNews(WmNews wmNews,short status,String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 抽取文章文本内容和图片链接
     * @param wmNews
     * @return Map<String,Object> key:content 文本内容 value: String key:images 图片链接 value:List<String>
     * */
    private Map<String,Object> handleTextAndImages(WmNews wmNews){
        Map<String,Object> result = new HashMap();
        String content = wmNews.getContent();
        if(StringUtils.isBlank(content)){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"文章内容不能为空");
        }
        //抽取文本内容
        List<Map> contensMapList = JSON.parseArray(content, Map.class);

        //拼接文章标题和文章内容
        String contents = contensMapList.stream()
                .filter(m -> m.get("type").equals("text"))
                .map(m->m.get("value").toString())
                .collect(Collectors.joining("_hmtt_"));
        contents=wmNews.getTitle()+ "_hmtt_"+ contents;
        result.put("content",contents);

        //抽取图片链接
        List<String> imagesList = contensMapList.stream()
                .filter(m -> m.get("type").equals("image"))
                .map(m->m.get("value").toString())
                .collect(Collectors.toList());
        //封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            List<String> coverImagesList = Arrays.stream(wmNews.getImages().split(","))
                    .map(url->webSite+url)
                    .collect(Collectors.toList());
            imagesList.addAll(coverImagesList);
        }
        imagesList = imagesList.stream().distinct().collect(Collectors.toList());
        result.put("images",imagesList);
        return result;
    }

}
