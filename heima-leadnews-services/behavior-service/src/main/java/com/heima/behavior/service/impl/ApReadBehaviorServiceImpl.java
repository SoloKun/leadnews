package com.heima.behavior.service.impl;

import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApReadBehaviorService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.ReadBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApReadBehavior;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
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
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {
    @Autowired
    private ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    private MongoTemplate mongoTemplate;
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
        //todo 通知es

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
