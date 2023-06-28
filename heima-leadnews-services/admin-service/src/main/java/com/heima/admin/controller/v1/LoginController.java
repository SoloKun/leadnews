package com.heima.admin.controller.v1;

import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdUserDTO;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: LoginController
 * Package: com.heima.admin.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 22:19
 * @Version 1.0
 */
@Api(tags = "管理员登录管理")
@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private AdUserService adUserService;
    @ApiOperation("登录")
    @ApiParam(name = "dto", value = "登录信息", required = true)
    @PostMapping("/in")
    public ResponseResult login(@RequestBody AdUserDTO dto){
        return adUserService.login(dto);
    }
}
