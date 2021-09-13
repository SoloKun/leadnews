package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.common.constants.admin.AdminConstants;
import com.heima.common.exception.CustException;
import com.heima.model.admin.dtos.AdUserDTO;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.admin.vos.AdUserVO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Version: V1.0
 */
@Service
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements AdUserService {
    /**
     * admin 登录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(AdUserDTO dto) {
        //1 参数校验
        if (dto == null || StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2 根据用户名查询用户信息
        AdUser adUser = getOne(Wrappers.<AdUser>lambdaQuery()
                .eq( AdUser::getName, dto.getName() )
                .eq(AdUser::getStatus, AdminConstants.AD_USER_ISSTATUS)
            );
        if (adUser == null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"用户名或密码错误");
        }
        //3 获取数据库密码和盐， 匹配密码
        String dbPwd = adUser.getPassword(); // 数据库密码（加密）
        String salt = adUser.getSalt();
        // 用户输入密码（加密后）
        String newPwd = DigestUtils.md5DigestAsHex((dto.getPassword() + salt).getBytes());
        if (!dbPwd.equals(newPwd)) {
            CustException.cust(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR,"用户名或密码错误");
        }
        //4 修改登录时间
        adUser.setLoginTime(new Date());
        updateById(adUser);

        //5 颁发token jwt 令牌
        String token = AppJwtUtil.getToken(adUser.getId().longValue());
        // 用户信息返回 VO
        AdUserVO userVO = new AdUserVO();
//        userVO.setId(adUser.getId());
//        userVO.setEmail(adUser.getEmail());
        BeanUtils.copyProperties(adUser, userVO);
        //6 返回结果（jwt）
        Map map = new HashMap();
        map.put("token", token);
        map.put("user", userVO);
        return ResponseResult.okResult(map);
    }
}