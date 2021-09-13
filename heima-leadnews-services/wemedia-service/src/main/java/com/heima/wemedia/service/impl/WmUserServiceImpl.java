package com.heima.wemedia.service.impl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmUserDTO;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmUserVO;
import com.heima.utils.common.AppJwtUtil;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.HashMap;
import java.util.Map;
@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
    /**
     * 登录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(WmUserDTO dto) {
        //1.检查参数
        if(StringUtils.isBlank(dto.getName())||StringUtils.isBlank(dto.getPassword())){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2 查询自媒体用户
        WmUser wmUser = getOne(Wrappers.<WmUser>lambdaQuery()
                .eq(WmUser::getName, dto.getName()));
        if (wmUser == null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if(wmUser.getStatus().intValue()!=9){// 可替换为常量
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"该用户状态异常，请联系管理员");
        }
        String pswd = DigestUtils.md5DigestAsHex((dto.getPassword() + wmUser.getSalt()).getBytes());
        if (!wmUser.getPassword().equals(pswd)) {
            CustException.cust(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        //4 返回jwt结果
        Map<String, Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(Long.valueOf(wmUser.getId())));
        WmUserVO userVO = new WmUserVO();
        BeanUtils.copyProperties(wmUser,userVO);
        map.put("user", userVO);
        return  ResponseResult.okResult(map);
    }
}