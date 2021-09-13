package com.heima.wemedia;

import com.heima.common.constants.message.NewsAutoScanConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

/**
 * @作者 itcast
 * @创建日期 2021/9/10 11:14
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class MqTest {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    public void publishNow(){
        // 演示 已到时间需要立即发布的文章
        rabbitTemplate.convertAndSend("publish.article.topic", "publish.article","待发布文章id: 1");
    }
    @Test
    public void publish3minLater(){
        // 演示 需要1分钟后发布的文章
        rabbitTemplate.convertAndSend("publish.article.topic", "dead.publish.article", "消息发送时间:" + LocalDateTime.now().toString(), new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setExpiration("60000");// 消息失效时间设置   单位: ms  到达失效时间 消息变为死信
                return message;
            }
        });
    }
}
