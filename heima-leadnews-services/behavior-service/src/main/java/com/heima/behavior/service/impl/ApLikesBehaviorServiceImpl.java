package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApLikesBehaviorService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.LikesBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApLikesBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.HotArticleConstants;
import com.heima.model.mess.app.NewBehaviorDTO;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * ClassName: ApLikesBehaviorServiceImpl
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 17:08
 * @Version 1.0
 */
@Service
@Slf4j
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public ResponseResult like(LikesBehaviorDTO dto) {
        ApUser apUser = AppThreadLocalUtils.getUser();
        if(apUser==null){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        Integer userId = apUser.getId();
        if(userId==0){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        Integer equipmentId = dto.getEquipmentId();
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(userId, equipmentId);
        if(apBehaviorEntry==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"行为实体不存在");
        }
        if(!apBehaviorEntry.isUser()){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        String entryId = apBehaviorEntry.getId();
        Short operation = dto.getOperation();
        Long articleId = dto.getArticleId();
        Short type = dto.getType();
        if(operation.intValue()==1){
            mongoTemplate.remove(Query.query(Criteria.where("entryId").is(entryId)
                    .and("articleId").is(articleId)
                    .and("type").is(type)), ApLikesBehavior.class);
            NewBehaviorDTO mess = new NewBehaviorDTO();
            mess.setArticleId(articleId);
            mess.setType(NewBehaviorDTO.BehaviorType.LIKES);
            mess.setAdd(-1);
            rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                    JSON.toJSONString(mess));
            log.info("发送mq消息:{}",JSON.toJSONString(mess));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }else{
            LikesBehaviorDTO likesBehaviorDTO = mongoTemplate.findOne(
                    Query.query(Criteria.where("entryId").is(entryId)
                            .and("articleId").is(articleId)
                            .and("type").is(type)), LikesBehaviorDTO.class);

            if(likesBehaviorDTO!=null){
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"已经点赞");
            }
            ApLikesBehavior newlikesBehaviorDTO = new ApLikesBehavior();
            BeanUtils.copyProperties(dto,newlikesBehaviorDTO);
            newlikesBehaviorDTO.setEntryId(entryId);
            newlikesBehaviorDTO.setCreatedTime(new Date());
            mongoTemplate.insert(newlikesBehaviorDTO);
            //将行为同步到mq中
            NewBehaviorDTO mess = new NewBehaviorDTO();
            mess.setArticleId(articleId);
            mess.setType(NewBehaviorDTO.BehaviorType.LIKES);
            mess.setAdd(1);
            rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                    JSON.toJSONString(mess));
            log.info("发送mq消息:{}",JSON.toJSONString(mess));
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
    }
}
