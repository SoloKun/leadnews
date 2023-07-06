package com.heima.search.service.impl;

import com.heima.common.exception.CustException;
import com.heima.feigns.BehaviorFeign;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDTO;
import com.heima.model.search.dtos.UserSearchDTO;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ClassName: ApUserSearchServiceiMPL
 * Package: com.heima.search.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 15:37
 * @Version 1.0
 */
@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    BehaviorFeign behaviorFeign;
    @Override
    @Async("taskExecutor")

    public void insert(UserSearchDTO userSearchDto) {
        //1 参数检查
        String searchWords = userSearchDto.getSearchWords();
        Integer userId = userSearchDto.getLoginUserId();
        ResponseResult<ApBehaviorEntry> behaviorResponse = behaviorFeign.findByUserIdOrEquipmentId(userId, userSearchDto.getEquipmentId());
        if (!behaviorResponse.checkCode()) {
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"BehaviorFeign findByUserIdOrEquipmentId 远程调用出错啦 ~~~ !!!! {} ");
        }
        ApBehaviorEntry apBehaviorEntry = behaviorResponse.getData();
        if (apBehaviorEntry==null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //3 查询当前用户搜索的关键词是否存在
        ApUserSearch apUserSearch = mongoTemplate.findOne(
                Query.query(Criteria.where("entryId").is(apBehaviorEntry.getId())
                        .and("keyword").is(searchWords)), ApUserSearch.class);

        //3.1 存在则更新最新时间，更新状态
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }
        //3.2 不存在则新增
        apUserSearch = new ApUserSearch();
        apUserSearch.setCreatedTime(new Date());
        apUserSearch.setEntryId(apBehaviorEntry.getId());
        apUserSearch.setKeyword(searchWords);
        mongoTemplate.save(apUserSearch);


    }

    @Override
    public ResponseResult findUserSearch(UserSearchDTO userSearchDto) {
        if(userSearchDto.getMinBehotTime() == null){
            userSearchDto.setMinBehotTime(new Date());
        }
        List<ApUserSearch> userSearchList = new ArrayList<>();
        int size = 10;
        ApUser user = AppThreadLocalUtils.getUser();
        ResponseResult<ApBehaviorEntry> behaviorResult = behaviorFeign.findByUserIdOrEquipmentId(user==null?null:user.getId(), userSearchDto.getEquipmentId());
        if (behaviorResult.getCode().intValue()!=0) {
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR,behaviorResult.getErrorMessage());
        }
        ApBehaviorEntry apBehaviorEntry = behaviorResult.getData();
        if (apBehaviorEntry == null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (apBehaviorEntry != null) {
            // 查询当前用户的搜索列表  按照时间倒序 .limit(userSearchDto.getPageSize())
            userSearchList = mongoTemplate.find
                    (Query.query(Criteria.where("entryId").is(apBehaviorEntry.getId())
                                    .and("createdTime")
                                    .lt(userSearchDto.getMinBehotTime()))
                    .with(Sort.by(Sort.Direction.DESC,"createdTime"))
                             .limit(size),ApUserSearch.class);
            return ResponseResult.okResult(userSearchList);
        }
        return ResponseResult.okResult(userSearchList);
    }

    @Override
    public ResponseResult delUserSearch(HistorySearchDTO historySearchDto) {
        if(historySearchDto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtils.getUser();
        ResponseResult<ApBehaviorEntry> behaviorEntryResult = behaviorFeign.findByUserIdOrEquipmentId(user == null ? null : user.getId(), historySearchDto.getEquipmentId());
        if(!behaviorEntryResult.checkCode()){
            return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"远程调用行为服务失败");
        }
        ApBehaviorEntry behaviorEntry = behaviorEntryResult.getData();
        if(behaviorEntry == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"行为实体数据不存在");
        }
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(historySearchDto.getId()).and("entryId").is(behaviorEntry.getId())),ApUserSearch.class);
        return ResponseResult.okResult();
    }
}
