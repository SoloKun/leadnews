package com.heima.admin.controller.v1;

import com.heima.admin.service.impl.AdsensitiveServiceImpl;
import com.heima.model.admin.dtos.SensitiveDTO;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AdSensitiveController
 * Package: com.heima.admin.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 19:54
 * @Version 1.0
 */

@Api(value = "敏感词管理",tags = "adSensitive",description = "敏感词管理API")
@RestController
@RequestMapping("/api/v1/sensitive")
public class AdSensitiveController {
    @Autowired
    private AdsensitiveServiceImpl adSensitiveService;
    @ApiOperation(value = "根据名称分页查询敏感词列表", notes = "查询敏感词列表")
    @ApiImplicitParam(name = "dto", value = "敏感词列表", required = true, dataType = "SensitiveDTO", paramType = "body")
    @PostMapping("/list")
    public ResponseResult list(@RequestBody SensitiveDTO dto){
        return adSensitiveService.list(dto);
    }
    @ApiOperation(value = "新增敏感词", notes = "新增敏感词")
    @ApiImplicitParam(name = "adSensitive", value = "敏感词", required = true, dataType = "AdSensitive", paramType = "body")
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdSensitive adSensitive){
        return adSensitiveService.insert(adSensitive);
    }
    @ApiOperation(value = "删除敏感词", notes = "删除敏感词")
    @ApiImplicitParam(name = "id", value = "敏感词id", required = true, dataType = "Integer", paramType = "path")
    @DeleteMapping("/del/{id}")
    public ResponseResult deleteById(@PathVariable("id") Integer id){
        return adSensitiveService.deleteById(id);
    }
    @ApiOperation(value = "修改敏感词", notes = "修改敏感词")
    @ApiImplicitParam(name = "adSensitive", value = "敏感词", required = true, dataType = "AdSensitive", paramType = "body")
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdSensitive adSensitive){
        return adSensitiveService.update(adSensitive);
    }

    @ApiOperation(value = "查询所有敏感词", notes = "查询所有敏感词")
    @PostMapping("/sensitives")
    public ResponseResult<List<String>> selectAllSensitives(){
        return adSensitiveService.selectAllSensitives();
    }
}
