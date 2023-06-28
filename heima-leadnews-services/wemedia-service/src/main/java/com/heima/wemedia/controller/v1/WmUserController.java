package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmUserDTO;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.impl.WmUserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: WmUserController
 * Package: com.heima.wemedia.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 15:12
 * @Version 1.0
 */
@Api(value = "自媒体用户管理",tags = "自媒体用户管理",description = "自媒体用户管理API")
@RestController
@RequestMapping("/api/v1/user")
public class WmUserController {
    @Autowired
    private WmUserServiceImpl wmUserServiceimpl;
    @ApiOperation("根据名称查询自媒体用户")
    @ApiParam(name = "name",value = "自媒体用户名称",required = true)
    @GetMapping("/findByName/{name}")
    public ResponseResult findByName(@PathVariable("name") String name) {
        return wmUserServiceimpl.findByName(name);
    };
    @ApiOperation("保存自媒体用户")
    @ApiParam(name = "wmUser",value = "自媒体用户",required = true)
    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmUser wmUser){
        return wmUserServiceimpl.saveUser(wmUser);
    }


}
