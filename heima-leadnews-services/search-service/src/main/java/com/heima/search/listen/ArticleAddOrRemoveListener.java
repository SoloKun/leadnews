package com.heima.search.listen;

import com.heima.feigns.ArticleFeign;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.constants.message.NewsUpOrDownConstants;
import com.heima.model.search.vos.SearchArticleVO;
import com.heima.search.service.ArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: ArticleAddOrRemoveListener
 * Package: com.heima.search.listen
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 13:54
 * @Version 1.0
 */
@Component
@Slf4j
public class ArticleAddOrRemoveListener {
    @Autowired
    ArticleFeign articleFeign;
    @Autowired
    ArticleSearchService articleSearchService;
    @RabbitListener(queuesToDeclare = @Queue(value = NewsUpOrDownConstants.NEWS_UP_FOR_ES_QUEUE))
    public void addArticle(String articleId){
        log.info("搜索微服务 接收到添加文章到索引库消息==> {}",articleId);
        ResponseResult<SearchArticleVO> articleVoResult = articleFeign.findArticle(Long.valueOf(articleId));
        if (!articleVoResult.checkCode()) {
            log.error("索引库添加失败 远程调用文章信息失败   文章id: {}",articleId);
        }
        SearchArticleVO searchArticleVo = articleVoResult.getData();
        if(searchArticleVo == null){
            log.error("索引库添加失败 未获取到对应文章信息   文章id: {}",articleId);
        }
        try {
            articleSearchService.saveArticle(searchArticleVo);
            log.info("成功更新索引信息   add: {}",articleId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消息消费确认失败   文章id: {}  原因: {}",articleId,e.getMessage());
        }
    }
    @RabbitListener(queuesToDeclare = @Queue(value = NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE))
    public void removeArticle(String articleId){
        log.info("搜索微服务 接收到删除索引库文章消息==> {}",articleId);
        try {
            articleSearchService.deleteArticle(articleId);
            log.info("成功更新索引信息   delete: {}",articleId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("消息消费确认失败   文章id: {}  原因: {}",articleId,e.getMessage());
        }
    }
}
