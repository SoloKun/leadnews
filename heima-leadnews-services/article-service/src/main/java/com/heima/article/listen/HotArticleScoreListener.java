package com.heima.article.listen;

import com.heima.model.constants.article.HotArticleConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 用于将监听到的行为数据
 * 存入到redis队列中 等待计算
 **/
@Component
@Slf4j
public class HotArticleScoreListener {
  @Autowired
  StringRedisTemplate redisTemplate;
  @RabbitListener(queuesToDeclare = {@Queue(value = HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE)})
  public void articleBehaviorHandler(String behaviorMess){
    log.info(" 文章实时更新队列: 接收到文章行为变化 消息内容: {}",behaviorMess);
    try {
      // 发布文章
      ListOperations<String, String> listOperations = redisTemplate.opsForList();
      listOperations.rightPush(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_LIST,behaviorMess);
      log.info(" 文章实时更新队列: 接收到文章行为变化 消息内容: {}",behaviorMess);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("文章实时更新队列， 消息处理失败: {}   失败原因: {}",behaviorMess,e.getMessage());
    }
  }
}