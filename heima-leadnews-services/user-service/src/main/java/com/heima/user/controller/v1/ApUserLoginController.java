package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDTO;
import com.heima.user.service.ApUserLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ApUserLoginController
 * Package: com.heima.user.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 16:25
 * @Version 1.0
 */
@Api(value = "用户登录管理",tags = "用户登录管理",description = "用户登录管理API")
@RestController
@RequestMapping("/api/v1/login")
public class ApUserLoginController {
    @Autowired
    ApUserLoginService apUserLoginService;
    @ApiOperation("app端登录")
    @ApiParam(name = "dto",value = "认证信息",required = true)
    @PostMapping("/login_auth")
    public ResponseResult loginAuth(@RequestBody LoginDTO dto) {
        return apUserLoginService.login(dto);
    }
}
