package com.heima.behavior.service.impl;

import com.heima.behavior.service.ApArticleBehaviorService;
import com.heima.behavior.service.ApBehaviorEntryService;
import com.heima.common.exception.CustException;
import com.heima.model.behavior.dtos.ArticleBehaviorDTO;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.behavior.pojos.ApCollection;
import com.heima.model.behavior.pojos.ApLikesBehavior;
import com.heima.model.behavior.pojos.ApUnlikesBehavior;
import com.heima.model.behavior.vos.ArticleBehaviorVO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.user.UserRelationConstants;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * ClassName: ApArticleBehaviorServiceImpl
 * Package: com.heima.behavior.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 22:01
 * @Version 1.0
 */
@Service
public class ApArticleBehaviorServiceImpl implements ApArticleBehaviorService {
    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public ResponseResult loadArticleBehavior(ArticleBehaviorDTO dto) {
        ArticleBehaviorVO articleBehaviorVO = new ArticleBehaviorVO(false, false, false, false);
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
        Query query = Query.query(Criteria.where("entryId").is(entryId)
                .and("articleId").is(articleId));
        ApLikesBehavior apLikesBehavior = mongoTemplate.findOne(query, ApLikesBehavior.class);
        if(apLikesBehavior==null){
            articleBehaviorVO.setIslike(false);
        }else{
            articleBehaviorVO.setIslike(true);
        }
        ApUnlikesBehavior apUnlikesBehavior = mongoTemplate.findOne(query, ApUnlikesBehavior.class);
        if(apUnlikesBehavior==null){
            articleBehaviorVO.setIsunlike(false);
        }else{
            articleBehaviorVO.setIsunlike(true);
        }
        ApCollection apCollection = mongoTemplate.findOne(query, ApCollection.class);
        if(apCollection==null){
            articleBehaviorVO.setIscollection(false);
        }else{
            articleBehaviorVO.setIscollection(true);
        }

        Integer authorId = dto.getAuthorId();
        Double score = stringRedisTemplate.opsForZSet()
                .score(UserRelationConstants.FOLLOW_LIST+userId,String.valueOf(authorId));
        if(score==null){
            articleBehaviorVO.setIsfollow(false);
        }else{
            articleBehaviorVO.setIsfollow(true);
        }
        return ResponseResult.okResult(articleBehaviorVO);


    }
}
