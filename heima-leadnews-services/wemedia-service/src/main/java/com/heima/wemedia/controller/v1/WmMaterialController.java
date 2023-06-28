package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.constants.wemedia.WemediaConstants;
import com.heima.model.wemedia.dtos.WmMaterialDTO;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.impl.WmMaterialServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * ClassName: WmMaterialController
 * Package: com.heima.wemedia.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 0:48
 * @Version 1.0
 */
@Api(value = "自媒体素材管理",tags = "自媒体素材管理",description = "自媒体素材管理API")
@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {

    @Autowired
    private WmMaterialServiceImpl wmMaterialServiceimpl;
    @ApiOperation("上传图片")
    @ApiParam(name = "multipartFile",value = "图片",required = true)
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialServiceimpl.uploadPicture(multipartFile);
    }
    @ApiOperation("删除图片")
    @ApiParam(name = "wmMaterial",value = "图片",required = true)
    @GetMapping("del_picture/{id}")
    public ResponseResult delPicture(@PathVariable("id") Integer id){
        return wmMaterialServiceimpl.delPicture(id);
    }
    @ApiOperation("查询图片列表")
    @ApiParam(name = "dto",value = "图片",required = true)
    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmMaterialDTO wmMaterialDTO){
        return wmMaterialServiceimpl.findList(wmMaterialDTO);
    }
    @ApiOperation("取消收藏素材")
    @ApiParam(name = "id",value = "图片",required = true)
    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollection(@PathVariable("id") Integer id){
        return wmMaterialServiceimpl.updateStatus(id, WemediaConstants.CANCEL_COLLECT_MATERIAL);
    }

    @ApiOperation("收藏素材")
    @ApiParam(name = "id",value = "图片",required = true)
    @GetMapping("/collect/{id}")
    public ResponseResult collection(@PathVariable("id") Integer id){
        return wmMaterialServiceimpl.updateStatus(id,WemediaConstants.COLLECT_MATERIAL);
    }

}
