package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApCollectionBehaviorService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.CollectionBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApCollection;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.HotArticleConstants;
import com.heima.model.mess.app.NewBehaviorDTO;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * ClassName: ApCollectionBehaviorServiceImpl
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 21:36
 * @Version 1.0
 */
@Service
@Slf4j
public class ApCollectionBehaviorServiceImpl implements ApCollectionBehaviorService {
    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    public ResponseResult collectBehavior(CollectionBehaviorDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        Integer userId = user.getId();
        if(userId==0){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        Integer equipmentId = dto.getEquipmentId();
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(userId, equipmentId);
        if(apBehaviorEntry==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"行为实体不存在");
        }
        if(!apBehaviorEntry.isUser()){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"行为实体不存在");
        }
        String entryId = apBehaviorEntry.getId();
        Long articleId = dto.getArticleId();
        Short type = dto.getType();
        Short operation = dto.getOperation();
        Query query = Query.query(Criteria.where("entryId").is(entryId)
                .and("articleId").is(articleId));
        ApCollection apCollection = mongoTemplate.findOne(query, ApCollection.class);
        if(operation==1) {//取消收藏
            if(apCollection==null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"收藏记录不存在");
            }
            mongoTemplate.remove(query,ApCollection.class);
            NewBehaviorDTO mess = new NewBehaviorDTO();
            mess.setArticleId(articleId);
            mess.setType(NewBehaviorDTO.BehaviorType.COLLECTION);
            mess.setAdd(-1);
            rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                    JSON.toJSONString(mess));
            log.info("发送mq消息:{}",JSON.toJSONString(mess));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }else{//收藏
            if(apCollection!=null){
                CustException.cust(AppHttpCodeEnum.DATA_EXIST,"收藏记录已存在");
            }
            apCollection = new ApCollection();
            apCollection.setArticleId(articleId);
            apCollection.setEntryId(entryId);
            apCollection.setType(type);
            apCollection.setCollectionTime(new Date());
            mongoTemplate.insert(apCollection);
            //将行为同步到mq中
            NewBehaviorDTO mess = new NewBehaviorDTO();
            mess.setArticleId(articleId);
            mess.setType(NewBehaviorDTO.BehaviorType.COLLECTION);
            mess.setAdd(1);
            rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                    JSON.toJSONString(mess));
            log.info("发送mq消息:{}",JSON.toJSONString(mess));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
    }
}
