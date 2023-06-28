package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmUserDTO;
import com.heima.wemedia.service.impl.WmUserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: LoginController
 * Package: com.heima.wemedia.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/17 20:51
 * @Version 1.0
 */
@Api(value = "自媒体用户登录管理",tags = "自媒体用户登录管理",description = "自媒体用户登录管理API")
@RestController
@RequestMapping("login")
public class LoginController {
    @Autowired
    private WmUserServiceImpl wmUserServiceimpl;
    @ApiOperation("自媒体用户登录")
    @ApiParam(name = "dto",value = "自媒体用户",required = true)
    @PostMapping("/in")
    public ResponseResult login(@RequestBody WmUserDTO wmUser){
        return wmUserServiceimpl.login(wmUser);
    }
}
