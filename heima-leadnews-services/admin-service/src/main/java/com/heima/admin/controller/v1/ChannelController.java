package com.heima.admin.controller.v1;

import com.heima.admin.service.ChannelService;
import com.heima.model.admin.dtos.ChannelDTO;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: ChannelController
 * Package: com.heima.admin.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/13 21:49
 * @Version 1.0
 */
@Api(value = "频道管理",tags = "channel",description = "频道管理API")
@RestController
@RequestMapping("/api/v1/channel")
public class ChannelController {
    @Autowired
    private ChannelService channelService;
    @ApiOperation(value = "根据名称分页查询频道列表", notes = "条件 1、名称 2、状态 以order排序")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "dto", value = "频道列表", required = true, dataType = "ChannelDTO", paramType = "body")
    )
    @PostMapping("/list")
    public ResponseResult list(@RequestBody ChannelDTO dto){
        return channelService.findByNameAndPage(dto);
    }

    @ApiOperation(value = "新增频道", notes = "新增频道")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "channel", value = "频道", required = true, dataType = "AdChannel", paramType = "body")
    )
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannel channel){
        return channelService.insert(channel);
    }
    @ApiOperation(value = "修改频道", notes = "修改频道")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "channel", value = "频道", required = true, dataType = "AdChannel", paramType = "body")
    )
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel channel){
        return channelService.update(channel);
    }
    @ApiOperation(value = "删除频道", notes = "删除频道")
    @ApiImplicitParams(
            @ApiImplicitParam(name = "id", value = "频道id", required = true, dataType = "Integer", paramType = "path")
    )
    @GetMapping("/del/{id}")
    public ResponseResult deleteById(@PathVariable("id") Integer id){

        return channelService.deleteById(id);
    }
    @ApiOperation("查询所有频道")
    @GetMapping("/channels")
    public ResponseResult findAll(){
        return channelService.findAll();
    }


    @ApiOperation("根据id查询频道 Article端调用")
    @GetMapping("/one/{id}")
    public ResponseResult findOne(@PathVariable Integer id) {
        return ResponseResult.okResult(channelService.getById(id));
    }

}
