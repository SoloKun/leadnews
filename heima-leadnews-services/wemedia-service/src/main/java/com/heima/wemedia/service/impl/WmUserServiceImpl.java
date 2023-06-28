package com.heima.wemedia.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.wemedia.WemediaConstants;
import com.heima.model.wemedia.dtos.WmNewsDTO;
import com.heima.model.wemedia.dtos.WmUserDTO;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmUserVO;
import com.heima.utils.common.AppJwtUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: WmUserServiceImpl
 * Package: com.heima.wemedia.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 15:02
 * @Version 1.0
 */
@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
    @Override
    public ResponseResult findByName(String name) {

        WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, name));
        return ResponseResult.okResult(wmUser);
    }

    @Override
    public ResponseResult saveUser(WmUser wmUser) {
        save(wmUser);
        return ResponseResult.okResult(wmUser);
    }

    @Override
    public ResponseResult login(WmUserDTO dto) {
        if(StringUtils.isBlank(dto.getName())||StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"用户名或密码为空");
        }
        WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getName, dto.getName()));
        if(wmUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
        }
        if(wmUser.getStatus().intValue()!=9){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_ALLOW,"用户已被禁用");
        }
        String password = DigestUtils.md5Hex((dto.getPassword()+wmUser.getSalt()).getBytes());
        if(!password.equals(wmUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(Long.valueOf(wmUser.getId())));
        map.put("user",wmUser);
        WmUserVO wmUserVO = new WmUserVO();
        BeanUtils.copyProperties(wmUser,wmUserVO);
        map.put("user",wmUserVO);
        return ResponseResult.okResult(map);
    }






}
