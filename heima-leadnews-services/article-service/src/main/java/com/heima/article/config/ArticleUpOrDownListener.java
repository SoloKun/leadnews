package com.heima.article.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.impl.ApArticleServiceImpl;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.constants.message.NewsUpOrDownConstants;
import com.heima.model.constants.message.PublishArticleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: ArticleUpOrDownListener
 * Package: com.heima.article.config
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/21 20:16
 * @Version 1.0
 */
@Slf4j
@Component
public class ArticleUpOrDownListener {
    @Autowired
    private ApArticleConfigService apArticleConfigService;
    @RabbitListener(queues=NewsUpOrDownConstants.NEWS_UP_FOR_ARTICLE_CONFIG_QUEUE)
    public void upHandler(String articleId){
        log.info("接收到文章上架通知， 待处理文章id: {} , 当前时间: {}",articleId);
        try {
            apArticleConfigService.update(Wrappers.<ApArticleConfig>lambdaUpdate()
                    .set(ApArticleConfig::getIsDown, false)
                            .eq(ApArticleConfig::getArticleId, Long.valueOf(articleId))
                    );
            log.info("文章上架通知处理完毕");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文章上架通知处理失败，文章id: {} , 失败原因:{}",articleId,e.getMessage());
        }
    }
    @RabbitListener(queues=NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE)
    public void downHandler(String articleId){
        log.info("接收到文章下架通知， 待处理文章id: {} , 当前时间: {}",articleId);
        try {
            apArticleConfigService.update(Wrappers.<ApArticleConfig>lambdaUpdate()
                    .set(ApArticleConfig::getIsDown, true)
                    .eq(ApArticleConfig::getArticleId, Long.valueOf(articleId))
            );
            log.info("文章下架通知处理完毕");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文章下架通知处理失败，文章id: {} , 失败原因:{}",articleId,e.getMessage());
        }
    }

}
