package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDTO;
import com.heima.user.service.ApUserRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: UserRelationController
 * Package: com.heima.user.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 21:31
 * @Version 1.0
 */
@Api(value = "用户关注",tags = "用户关注管理")
@RestController
@RequestMapping("/api/v1/user")
public class UserRelationController {
    @Autowired
    private ApUserRelationService apUserRelationService;
    @ApiOperation("关注或取消关注")
    @PostMapping("/user_follow")
    public ResponseResult follow(@RequestBody UserRelationDTO dto){
        return apUserRelationService.follow(dto);
    }
}
