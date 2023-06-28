package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.constants.admin.AdminConstants;
import com.heima.model.user.dtos.AuthDTO;
import com.heima.model.user.dtos.LoginDTO;
import com.heima.user.service.ApUserLoginService;
import com.heima.user.service.impl.ApUserRealnameServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ApUserRealnameController
 * Package: com.heima.user.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 23:16
 * @Version 1.0
 */
@Api(value = "用户认证信息管理",tags = "用户认证信息管理",description = "用户认证信息管理API")
@RestController
@RequestMapping("/api/v1/auth")
public class ApUserRealnameController {
    @Autowired
    private ApUserRealnameServiceImpl apUserRealnameServiceimpl;
    @ApiOperation("根据状态查询认证列表")
    @ApiParam(name = "dto",value = "认证信息",required = true)
    @PostMapping("/list")
    public ResponseResult loadListByStatus(@RequestBody AuthDTO dto){
        return apUserRealnameServiceimpl.loadListByStatus(dto);
    }

    @ApiOperation("实名认证通过")
    @ApiParam(name = "dto",value = "认证信息",required = true)
    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDTO DTO) {
        return apUserRealnameServiceimpl.updateStatusById(DTO, AdminConstants.PASS_AUTH);
    }
    @ApiOperation("实名认证失败")
    @ApiParam(name = "dto",value = "认证信息",required = true)
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDTO DTO) {
        return apUserRealnameServiceimpl.updateStatusById(DTO, AdminConstants.FAIL_AUTH);
    }



}
