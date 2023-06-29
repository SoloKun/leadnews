package com.heima.behavior.service.impl;

import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.common.exception.CustException;
import com.heima.common.exception.CustomException;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * ClassName: ApBehaviorEntryServiceImpl
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 16:29
 * @Version 1.0
 */
@Service
public class ApBehaviorEntryServiceImpl implements ApBehaviorEntryService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer equipmentId) {
        if(userId!=null){
            Query query = Query.query
                    (Criteria.where("refId").
                            is(userId).and("type").
                            is(ApBehaviorEntry.Type.USER.getCode()));
            ApBehaviorEntry behaviorEntry = mongoTemplate.findOne(query, ApBehaviorEntry.class);
            if(behaviorEntry==null){
                ApBehaviorEntry entry = new ApBehaviorEntry();
                entry.setRefId(userId);
                entry.setType(ApBehaviorEntry.Type.USER.getCode());
                mongoTemplate.insert(entry);
                return entry;
            }else return behaviorEntry;
        }else{
            if(equipmentId==null){
               return null;
            }
            Query query = Query.query
                    (Criteria.where("refId").
                            is(equipmentId).and("type").
                            is(ApBehaviorEntry.Type.EQUIPMENT.getCode()));
            ApBehaviorEntry behaviorEntry = mongoTemplate.findOne(query, ApBehaviorEntry.class);
            if(behaviorEntry==null){
                ApBehaviorEntry entry = new ApBehaviorEntry();
                entry.setRefId(equipmentId);
                entry.setType(ApBehaviorEntry.Type.EQUIPMENT.getCode());
                mongoTemplate.insert(entry);
                return entry;
            }
            else return behaviorEntry;
        }
    }
}
