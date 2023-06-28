package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.constants.wemedia.WemediaConstants;
import com.heima.model.wemedia.dtos.NewsAuthDTO;
import com.heima.model.wemedia.dtos.WmNewsDTO;
import com.heima.model.wemedia.dtos.WmNewsPageReqDTO;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.impl.WmNewsServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: WmNewsController
 * Package: com.heima.wemedia.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 23:40
 * @Version 1.0
 */
@Api(value = "自媒体文章管理", tags = "自媒体文章管理", description = "自媒体文章管理API")
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsServiceImpl wmNewsService;

    @ApiOperation("自媒体文章列表")
    @ApiParam(name = "dto", value = "自媒体文章列表", required = true)
    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDTO dto) {
        return wmNewsService.findList(dto);
    }

    @ApiOperation("发布文章")
    @ApiParam(name = "dto", value = "发布文章", required = true)
    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDTO dto) {
        return wmNewsService.submitNews(dto);
    }

    @ApiOperation("修改文章")
    @ApiParam(name = "id", value = "文章id", required = true)
    @GetMapping("/one/{id}")
    public ResponseResult findWmNewsById(@PathVariable("id") Integer id) {
        return wmNewsService.findWmNewsById(id);
    }

    @ApiOperation("删除文章")
    @ApiParam(name = "id", value = "文章id", required = true)
    @GetMapping("/del_news/{id}")
    public ResponseResult delNews(@PathVariable("id") Integer id) {
        return wmNewsService.delNews(id);
    }

    @ApiOperation("上下架文章")
    @ApiParam(name = "dto", value = "上下架文章", required = true)
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDTO dto) {
        return wmNewsService.downOrUp(dto);
    }

    @ApiOperation("查询文章列表加作者 admin端查询")
    @ApiParam(name = "dto", value = "查询文章列表加作者", required = true)
    @PostMapping("/list_vo")
    public ResponseResult findList(@RequestBody NewsAuthDTO dto) {
        return wmNewsService.findList(dto);
    }

    @ApiOperation("查询文章详情 admin端查询")
    @ApiParam(name = "id", value = "文章id", required = true)
    @GetMapping("/one_vo/{id}")
    public ResponseResult findWmNewsVoById(@PathVariable("id") Integer id) {
        return wmNewsService.findWmNewsVo(id);
    }
    @ApiOperation("审核文章通过 4")
    @ApiParam(name = "dto", value = "审核文章通过", required = true)
    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDTO dto){
        return wmNewsService.updateStatus(WemediaConstants.WM_NEWS_AUTH_PASS,dto);
    }

    @ApiOperation("审核文章失败 2")
    @ApiParam(name = "dto", value = "审核文章失败", required = true)
    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDTO dto){
        return wmNewsService.updateStatus(WemediaConstants.WM_NEWS_AUTH_FAIL,dto);
    }

    @ApiOperation("根据id修改文章 Article端远程调用")
    @ApiParam(name = "id", value = "文章id", required = true)
    @PutMapping("/update")
    public ResponseResult updateWmNews(@RequestBody WmNews wmNews){
        wmNewsService.updateById(wmNews);
        return ResponseResult.okResult();
    }


}
