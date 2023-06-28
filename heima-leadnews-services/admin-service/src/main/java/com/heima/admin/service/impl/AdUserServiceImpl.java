package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.common.exception.CustException;
import com.heima.model.admin.dtos.AdUserDTO;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.admin.vos.AdUserVO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: AdUserServiceImpl
 * Package: com.heima.admin.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 22:00
 * @Version 1.0
 */
@Service
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper,AdUser> implements AdUserService {

    @Transactional
    @Override
    public ResponseResult login(AdUserDTO dto) {
        String name = dto.getName();
        String password = dto.getPassword();
        if(name == null || password == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"用户名或密码错误");
        }
        AdUser user = this.getOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, name));
        if(user == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"用户名或密码错误");
        }
        String inputPwd = DigestUtils.md5DigestAsHex((password + user.getSalt()).getBytes());
        if(!user.getPassword().equals(inputPwd)){
            CustException.cust(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR,"用户名或密码错误");
        }
        if(user.getStatus().intValue()!=9){
            CustException.cust(AppHttpCodeEnum.LOGIN_STATUS_ERROR,"用户已被禁用");
        }
        user.setLoginTime(new Date());
        this.updateById(user);
        String token = AppJwtUtil.getToken(Long.valueOf(user.getId()));
        Map result = new HashMap<>();
        result.put("token",token);
        AdUserVO adUserVO = new AdUserVO();
        BeanUtils.copyProperties(user,adUserVO);
        result.put("user",adUserVO);
        return ResponseResult.okResult(result);
    }
}
