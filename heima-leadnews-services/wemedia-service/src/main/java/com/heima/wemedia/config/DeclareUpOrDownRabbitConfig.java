package com.heima.wemedia.config;

import com.heima.model.constants.message.NewsUpOrDownConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: DeclareUpOrDownRabbitConfig
 * Package: com.heima.wemedia.config
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/21 20:04
 * @Version 1.0
 */
@Configuration
/**
 * 声明上下架文章所需的 所有交换机  队列 及 绑定关系
 */
public class DeclareUpOrDownRabbitConfig {
    @Bean
    public TopicExchange wmNewsUpOrDownExchange(){
        return new TopicExchange(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE);
    }
    @Bean
    public Queue newsUpForArticleConfigQueue(){
        return new Queue(NewsUpOrDownConstants.NEWS_UP_FOR_ARTICLE_CONFIG_QUEUE,true);
    }
    @Bean
    public Queue newsDownForArticleConfigQueue(){
        return new Queue(NewsUpOrDownConstants.NEWS_DOWN_FOR_ARTICLE_CONFIG_QUEUE,true);
    }
    @Bean
    public Queue newsUpForEsQueue(){
        return new Queue(NewsUpOrDownConstants.NEWS_UP_FOR_ES_QUEUE,true);
    }
    @Bean
    public Queue newsDownForEsQueue(){
        return new Queue(NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE,true);
    }

    @Bean
    public Binding bindingNewsUpForArticleConfigQueue(){
        return new Binding(NewsUpOrDownConstants.NEWS_UP_FOR_ARTICLE_CONFIG_QUEUE, Binding.DestinationType.QUEUE,
                NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY,null);
    }
    @Bean
    public Binding bindingNewsDownForArticleConfigQueue(){
        return new Binding(NewsUpOrDownConstants.NEWS_DOWN_FOR_ARTICLE_CONFIG_QUEUE, Binding.DestinationType.QUEUE,
                NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY,null);
    }
    @Bean
    public Binding bindingNewsUpForEsQueue(){
        return new Binding(NewsUpOrDownConstants.NEWS_UP_FOR_ES_QUEUE, Binding.DestinationType.QUEUE,
                NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY,null);
    }
    @Bean
    public Binding bindingNewsDownForEsQueue(){
        return new Binding(NewsUpOrDownConstants.NEWS_DOWN_FOR_ES_QUEUE, Binding.DestinationType.QUEUE,
                NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY,null);
    }

}
