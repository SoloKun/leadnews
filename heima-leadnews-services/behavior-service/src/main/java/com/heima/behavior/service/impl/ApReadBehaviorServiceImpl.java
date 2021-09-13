package com.heima.behavior.service.impl;
import java.util.Date;
import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.behavior.service.ApReadBehaviorService;
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
@Service
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {

    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ResponseResult readBehavior(ReadBehaviorDTO dto) {
        // 1. 校验参数
        ApUser user = AppThreadLocalUtils.getUser();
        // 2. 查询行为实体
        ApBehaviorEntry apBehaviorEntry = apBehaviorEntryService.findByUserIdOrEquipmentId(user==null?null:user.getId(), dto.getEquipmentId());
        if (apBehaviorEntry == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"获取行为实体信息失败");
        }
        // 3. 查询指定文章阅读行为是否存在
        Query query = Query.query(Criteria.where("entryId").is(apBehaviorEntry.getId()).and("articleId").is(dto.getArticleId()));
        ApReadBehavior readBehavior = mongoTemplate.findOne(query, ApReadBehavior.class);
        if(readBehavior == null){
            // 不存在    新增阅读行为
            readBehavior = new ApReadBehavior();
            readBehavior.setEntryId(apBehaviorEntry.getId());
            readBehavior.setArticleId(dto.getArticleId());
            readBehavior.setCount((short)1);
            readBehavior.setCreatedTime(new Date());
            readBehavior.setUpdatedTime(new Date());
            mongoTemplate.save(readBehavior);
        }else {
            // 存在     更新阅读次数
            readBehavior.setCount((short)(readBehavior.getCount() + 1));
            mongoTemplate.save(readBehavior);
        }
        return ResponseResult.okResult();
    }
}
