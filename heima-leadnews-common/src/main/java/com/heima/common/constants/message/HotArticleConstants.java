package com.heima.common.constants.message;
public class HotArticleConstants {
    // 所有计算热点文章的行为操作，都会往这个主题发消息
    public static final String HOTARTICLE_SCORE_INPUT_TOPIC="hot.article.score.topic";

    // 计算文章分值成功后发送消息topic
    public static final String HOTARTICLE_INCR_HANDLE_OUPUT_TOPIC="hot.article.incr.handle.topic";
}