package com.heima.wemedia.listen;
import com.heima.common.constants.message.NewsAutoScanConstants;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.io.IOException;
@Component
@Slf4j
public class WemediaNewsAutoListener {
    @Autowired
    WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    WmNewsService wmNewsService;
    /**
     * 消费方法， 可能会因为重试发送 或 异常重试机制而多次消费
     * 需要考虑方法的幂等性
     * @param newsId 消息内容   文章id
     * @param message   原生消息对象
     * @param channel   原生信道对象
     */
    /**
     * queues: 监听指定队列
     * queuesToDeclare: 声明并监听指定队列
     * bindings: 声明队列  交换机  并通过路由绑定
     */
    @RabbitListener(queuesToDeclare = {@Queue(name = NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC)})
    public void newsAutoScanHandler(@Payload String newsId, Message message, Channel channel){
        log.info("接收到 自动审核 消息===> {}",newsId);
        try {
            // 自动审核
            wmNewsAutoScanService.autoScanWmNews(Integer.valueOf(newsId));
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(" 消费消息出现异常  ， 原因: {}" ,e.getMessage());
        }
    }
}
