package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleHomeDTO;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.ArticleConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "app端首页文章列表接口",value = "app端首页文章列表接口")
@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {
    @Autowired
    private ApArticleService articleService;
    @ApiOperation(value = "查询热点文章列表",notes = "第一次点击各类频道时，调用此接口 查询该频道对应热点文章")
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDTO dto) {
        // return articleService.load(ArticleConstants.LOADTYPE_LOAD_MORE,dto );
        return articleService.load2(ArticleConstants.LOADTYPE_LOAD_MORE,dto,true);
    }
    @ApiOperation(value = "查询更多文章",notes = "app文章列表下拉时 根据页面文章最小时间 实现滚动查询")
    @PostMapping("/loadmore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDTO dto) {
        return articleService.load(ArticleConstants.LOADTYPE_LOAD_MORE,dto);
    }
    @ApiOperation(value = "查询最新文章",notes = "app文章列表上拉时 根据页面文章最大时间 查询最新文章")
    @PostMapping("/loadnew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDTO dto) {
        return articleService.load(ArticleConstants.LOADTYPE_LOAD_NEW,dto);
    }

    @ApiOperation(value = "根据文章id查询文章信息")
    @GetMapping("/findById/{id}")
    public ResponseResult findById(@PathVariable("id") Long id) {
        if (id == null || id <= 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "参数有误");
        }
        ApArticle apArticle = articleService.getById(id);
        if (apArticle == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        return ResponseResult.okResult(apArticle);
    }
}