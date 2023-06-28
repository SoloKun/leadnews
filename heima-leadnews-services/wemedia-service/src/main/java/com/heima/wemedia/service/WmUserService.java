package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDTO;
import com.heima.model.wemedia.dtos.WmUserDTO;
import com.heima.model.wemedia.pojos.WmUser;

/**
 * ClassName: WmUserService
 * Package: com.heima.wemedia.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 15:01
 * @Version 1.0
 */
public interface WmUserService extends IService<WmUser> {
    public ResponseResult findByName(String name);

    public ResponseResult saveUser(WmUser wmUser);

    public ResponseResult login(WmUserDTO dto);




}
