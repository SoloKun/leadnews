package com.heima.common.constants.message;
/**
 * 发布文章相关常量
 **/
public class PublishArticleConstants {
    // 发布文章交换机
    public static final String PUBLISH_ARTICLE_EXCHANGE = "publish.article.topic";
    // 死信交换机
    public static final String DEAD_PUBLISH_ARTICLE_EXCHANGE = "dead.topic";
    // 死信队列
    public static final String DEAD_PUBLISH_ARTICLE_QUEUE = "dead.publish.article.queue";
    // 发布文章队列
    public static final String PUBLISH_ARTICLE_QUEUE = "publish.article.queue";
    // 通往发布文章队列的路由key
    public static final String PUBLISH_ARTICLE_ROUTE_KEY = "publish.article";
    // 通往死信队列的路由key
    public static final String PUBLISH_ARTICLE_DEAD_ROUTE_KEY = "dead.publish.article";
}
