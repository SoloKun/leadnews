package com.heima.article.listen;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.message.PublishArticleConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
@Component
@Slf4j
public class PublishArticleListener {
    @Autowired
    private ApArticleService apArticleService;
    @RabbitListener(queuesToDeclare = {@Queue(value = PublishArticleConstants.PUBLISH_ARTICLE_QUEUE)})
    public void publishArticle(@Payload String newsId, Message message, Channel channel){
        log.info("接收到发布文章通知， 待发布文章id: {} , 当前时间: {}",newsId, LocalDateTime.now().toString());
        try {
            // 发布文章
            apArticleService.publishArticle(Integer.valueOf(newsId));
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("发布文章通知处理完毕  文章发布成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发布文章通知处理失败， 文章未能成功发布 文章id: {} , 失败原因:{}",newsId,e.getMessage());
        }
    }
}
