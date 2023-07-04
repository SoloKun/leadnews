package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApReadBehaviorService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.ReadBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApReadBehavior;
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
 * ClassName: readBehavior
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 20:16
 * @Version 1.0
 */
@Service
@Slf4j
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {
    @Autowired
    private ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public ResponseResult readBehavior(ReadBehaviorDTO dto) {
        Integer equipmentId = dto.getEquipmentId();
        ApUser user = AppThreadLocalUtils.getUser();
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(user.getId(), equipmentId);
        if(user==null){
            //未登录 游客
            apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(null, equipmentId);
        }else{
            //登录用户
            apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(user.getId(), equipmentId);
        }
        if(apBehaviorEntry==null){
            CustException.cust(AppHttpCodeEnum.DATA_EXIST,"行为实体不存在");
        }
        String entryId = apBehaviorEntry.getId();
        Long articleId = dto.getArticleId();
        Query query = Query.query(Criteria.where("entryId").is(entryId)
                .and("articleId").is(articleId));
        ApReadBehavior apReadBehavior = mongoTemplate.findOne(query, ApReadBehavior.class);
        if(apReadBehavior==null){
            apReadBehavior = new ApReadBehavior();
            apReadBehavior.setEntryId(entryId);
            apReadBehavior.setArticleId(articleId);
            apReadBehavior.setCount((short)1);
            apReadBehavior.setCreatedTime(new Date());
            apReadBehavior.setUpdatedTime(new Date());
            mongoTemplate.insert(apReadBehavior);
        }else{
            apReadBehavior.setCount((short)(apReadBehavior.getCount()+1));
            apReadBehavior.setUpdatedTime(new Date());
            mongoTemplate.save(apReadBehavior);
        }
        // 将行为同步到mq中
        NewBehaviorDTO mess = new NewBehaviorDTO();
        mess.setArticleId(articleId);
        mess.setAdd(1);
        mess.setType(NewBehaviorDTO.BehaviorType.VIEWS);
        rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                JSON.toJSONString(mess));
        log.info("发送mq消息：{}",JSON.toJSONString(mess));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
