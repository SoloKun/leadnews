package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDTO;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserLoginService;

import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: ApUserLoginServoceImpl
 * Package: com.heima.user.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 15:54
 * @Version 1.0
 */
@Service
public class ApUserLoginServoceImpl implements ApUserLoginService {
    @Autowired
    ApUserMapper apUserMapper;


    @Override
    public ResponseResult login(LoginDTO dto) {
        String phone = dto.getPhone();
        String password = dto.getPassword();
        //1.参数校验
        if(StringUtils.isNotBlank(phone) && StringUtils.isNotBlank(password)){
            //2.根据手机号查询用户
            ApUser apUser = apUserMapper.selectOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, phone));
            if(ObjectUtils.isNull(apUser)){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
            }
            //3.校验密码
            String dbPassword = apUser.getPassword();
            String inputPassword = DigestUtils.md5DigestAsHex((password + apUser.getSalt()).getBytes());
            if(!dbPassword.equals(inputPassword)){
                CustException.cust(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR,"密码错误");
            }
            //4.生成token
            String token = AppJwtUtil.getToken(apUser.getId().longValue());
            Map res = new HashMap<>();
            apUser.setSalt(null);
            apUser.setPassword(null);
            res.put("token",token);
            res.put("user",apUser);
            return ResponseResult.okResult(res);
            //5.返回结果
        }else{
            //设备id登录
            if (dto.getEquipmentId()==null){
                CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"设备id不能为空");
            }
            String token = AppJwtUtil.getToken(0l);
            Map res = new HashMap<>();
            res.put("token",token);
            return ResponseResult.okResult(res);
        }

    }
}
