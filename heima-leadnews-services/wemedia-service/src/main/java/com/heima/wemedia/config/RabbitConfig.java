package com.heima.wemedia.config;
import com.heima.common.constants.message.NewsUpOrDownConstants;
import com.heima.common.constants.message.PublishArticleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * InitializingBean: springbean生命周期接口  代表完成bean装配后 执行的初始化方法
 * 这个类的目的：
 *     设置rabbitmq消息序列化机制  （默认jdk效率差）
 *     设置rabbitmq消息发送确认 回调
 *     设置rabbitmq消息返还 回调
 */
@Configuration
@Slf4j
public class RabbitConfig implements InitializingBean {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    public void afterPropertiesSet()  {
        log.info("初始化rabbitMQ配置 ");
        // 设置消息转换器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        // 设置发送确认 回调方法
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData 对比数据
             * @param ack  是否成功发送到mq exchange
             * @param cause  原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (!ack){
                    // TODO 可扩展自动重试

                    log.error("发送消息到mq失败  ，原因: {}",cause);
                }
            }
        });
        // 设置消息返还 回调方法
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * @param message  消息内容
             * @param replyCode  回复状态
             * @param replyText  回复文本提示
             * @param exchange   交换机
             * @param routingKey   路由
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                // TODO 可扩展自动重试

                log.error("消息返还回调触发  ，交换机: {} , 路由: {} , 消息内容: {} , 原因: {}  ",exchange,routingKey,message,replyText);
            }
        });
    }
    /**
     * 订单业务交换机
     */
    @Bean
    public TopicExchange articleTopicExchange(){
        return new TopicExchange(PublishArticleConstants.PUBLISH_ARTICLE_EXCHANGE, true, false);
    }
    /**
     * 死信交换机
     */
    @Bean
    public TopicExchange deadTopicExchange(){
        return new TopicExchange(PublishArticleConstants.DEAD_PUBLISH_ARTICLE_EXCHANGE, true, false);
    }
    /**
     * 死信队列
     */
    @Bean
    public Queue deadPublishArticleQueue(){
        Map<String, Object> args = new HashMap<>(2);
        // x-dead-letter-exchange 声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", PublishArticleConstants.DEAD_PUBLISH_ARTICLE_EXCHANGE);
        args.put("x-dead-letter-routing-key", PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY);
        return QueueBuilder.durable(PublishArticleConstants.DEAD_PUBLISH_ARTICLE_QUEUE).withArguments(args).build();
    }

    /**
     * 声明发布文章队列
     * @return
     */
    @Bean
    public Queue publishArticleQueue(){
        return new Queue(PublishArticleConstants.PUBLISH_ARTICLE_QUEUE, true);
    }
    /**
     * 绑定 发布文章交换机 + 死信队列
     * @return
     */
    @Bean
    public Binding bindingDeadQueue(){
        return BindingBuilder.bind(deadPublishArticleQueue()).to(articleTopicExchange()).with(PublishArticleConstants.PUBLISH_ARTICLE_DEAD_ROUTE_KEY);
    }
    /**
     * 绑定 发布文章交换机 + 发布文章队列
     * @return
     */
    @Bean
    public Binding bindingPublishArticleQueue(){
        return BindingBuilder.bind(publishArticleQueue()).to(articleTopicExchange()).with(PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY);
    }
    /**
     * 绑定 死信交换机 + 发布文章队列
     * @return
     */
    @Bean
    public Binding bindingPublishArticleQueue2(){
        return BindingBuilder.bind(publishArticleQueue()).to(deadTopicExchange()).with(PublishArticleConstants.PUBLISH_ARTICLE_ROUTE_KEY);
    }


    // ======================= 文章上下架 相关交换机及队列  START =========================
    @Bean
    public TopicExchange newsUpOrDownTopicExchange(){
        return new TopicExchange(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE, true, false);
    }
    @Bean
    public Queue newsUpForArticleConfig(){
        return new Queue(NewsUpOrDownConstants.NEWS_UP_FOR_ARTICLE_CONFIG_QUEUE, true);
    }
    @Bean
    public Queue newsUpForES(){
        return new Queue(NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE, true);
    }
    @Bean
    public Queue newsDownForArticleConfig(){
        return new Queue(NewsUpOrDownConstants.NEWS_DOWN_FOR_ARTICLE_CONFIG_QUEUE, true);
    }
    @Bean
    public Queue newsDownForES(){
        return new Queue(NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE, true);
    }
    @Bean
    public Binding bindingNewsUpForES(){
        return BindingBuilder.bind(newsUpForES()).to(newsUpOrDownTopicExchange()).with(NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY);
    }
    @Bean
    public Binding bindingNewsUpForArticleConfig(){
        return BindingBuilder.bind(newsUpForArticleConfig()).to(newsUpOrDownTopicExchange()).with(NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY);
    }
    @Bean
    public Binding bindingNewsDownForES(){
        return BindingBuilder.bind(newsDownForES()).to(newsUpOrDownTopicExchange()).with(NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY);
    }
    @Bean
    public Binding bindingNewsDownForArticleConfig(){
        return BindingBuilder.bind(newsDownForArticleConfig()).to(newsUpOrDownTopicExchange()).with(NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY);
    }
    // =======================  文章上下架 相关交换机及队列   END  =========================
}
