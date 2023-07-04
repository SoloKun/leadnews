package com.heima.behavior.service.impl;

import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApUnlikeBehaviorService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.UnLikesBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApUnlikesBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
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
 * ClassName: ApUnlikeBehaviorServiceImpl
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 21:06
 * @Version 1.0
 */
@Service
@Slf4j
public class ApUnlikeBehaviorServiceImpl implements ApUnlikeBehaviorService {
    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ResponseResult unlikeBehavior(UnLikesBehaviorDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if(user==null){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"未登录");
        }
        Integer userId = user.getId();
        if(userId==0){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"未登录");
        }
        Integer equipmentId = dto.getEquipmentId();
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(userId, equipmentId);
        if(apBehaviorEntry==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"行为实体不存在");
        }
        if(!apBehaviorEntry.isUser()){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN,"未登录");
        }
        Long articleId = dto.getArticleId();
        String entryId = apBehaviorEntry.getId();
        Short type = dto.getType();
        Query query = Query.query(Criteria.where("entryId").is(entryId)
                .and("articleId").is(articleId));
        ApUnlikesBehavior apUnlikesBehavior = mongoTemplate.findOne(query, ApUnlikesBehavior.class);

        if(ApUnlikesBehavior.Type.UNLIKE.getCode()==type) {
            if(apUnlikesBehavior!=null){
                CustException.cust(AppHttpCodeEnum.DATA_EXIST,"已经不喜欢了");
            }
            apUnlikesBehavior = new ApUnlikesBehavior();
            apUnlikesBehavior.setArticleId(articleId);
            apUnlikesBehavior.setEntryId(entryId);
            apUnlikesBehavior.setType(type);
            apUnlikesBehavior.setCreatedTime(new Date());
            mongoTemplate.insert(apUnlikesBehavior);


            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }else{
            if(apUnlikesBehavior==null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"没有不喜欢");
            }
            mongoTemplate.remove(query,ApUnlikesBehavior.class);
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

    }
}
