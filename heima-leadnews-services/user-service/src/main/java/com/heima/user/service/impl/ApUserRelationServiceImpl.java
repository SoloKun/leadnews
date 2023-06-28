package com.heima.user.service.impl;

import com.heima.common.exception.CustException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.user.UserRelationConstants;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.dtos.UserRelationDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApUserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;



/**
 * ClassName: ApUserRelationServiceImpl
 * Package: com.heima.user.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 21:00
 * @Version 1.0
 */
@Service
public class ApUserRelationServiceImpl implements ApUserRelationService {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 关注或取消关注
     * @param dto
     * @return
     */
    @Override
    public ResponseResult follow(UserRelationDTO dto) {
        ApUser loginUser = AppThreadLocalUtils.getUser();
        if(loginUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN,"请先登录");
        }
        if(dto.getAuthorApUserId()==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"作者对应的userId不存在");
        }
        Short operation = dto.getOperation();
        if(operation==null||(operation.intValue()!=0&&operation.intValue()!=1)){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"操作参数不合法");
        }
        Integer loginId = loginUser.getId();
        Integer followId = dto.getAuthorApUserId();
        if(loginId.equals(followId)){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"不能关注自己");
        }
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        try {
            if(operation==0) {
                Double score = zSetOperations.score(UserRelationConstants.FOLLOW_LIST + loginUser.getId().toString(), dto.getAuthorApUserId().toString());
                if(score!=null){
                    CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"已经关注过了");
                }
                zSetOperations.add(UserRelationConstants.FOLLOW_LIST + loginUser.getId().toString(),
                        dto.getAuthorApUserId().toString(), System.currentTimeMillis());
                zSetOperations.add(UserRelationConstants.FANS_LIST + dto.getAuthorApUserId().toString(),
                        loginUser.getId().toString(), System.currentTimeMillis());
            }else{
                zSetOperations.remove(UserRelationConstants.FOLLOW_LIST + loginUser.getId().toString(),
                        dto.getAuthorApUserId().toString());
                zSetOperations.remove(UserRelationConstants.FANS_LIST + dto.getAuthorApUserId().toString(),
                        loginUser.getId().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR,"服务器错误");
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
