package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AdUserDTO;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

/**
 * ClassName: AdUserService
 * Package: com.heima.admin.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 22:00
 * @Version 1.0
 */
public interface AdUserService extends IService<AdUser> {
/**
     * 登录
     * @param dto
     * @return data: {token, user}
     */
    public ResponseResult login(AdUserDTO dto);
}
