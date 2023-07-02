package com.heima.comment.controller.v1;

import com.heima.comment.service.CommentService;
import com.heima.model.comment.dtos.CommentDTO;
import com.heima.model.comment.dtos.CommentLikeDTO;
import com.heima.model.comment.dtos.CommentSaveDTO;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * ClassName: CommentController
 * Package: com.heima.comment.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/1 0:04
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/v1/comment")
@Api(value = "评论管理", tags = "评论管理")
public class CommentController {
    @Autowired
    CommentService commentService;
    @ApiOperation("保存评论")
    @ApiParam(name = "dto", value = "保存评论", required = true)
    @PostMapping("/save")
    public ResponseResult save(@RequestBody @Valid CommentSaveDTO dto) {
        return commentService.saveComment(dto);
    }
    @ApiOperation("点赞")
    @ApiParam(name = "dto", value = "点赞", required = true)
    @PostMapping("/like")
    public ResponseResult like(@RequestBody @Valid CommentLikeDTO dto) {
        return commentService.like(dto);
    }
    @ApiOperation("获取评论列表")
    @ApiParam(name = "dto", value = "获取评论列表", required = true)
    @PostMapping("/load")
    public ResponseResult load(@RequestBody @Valid CommentDTO dto) {
        return commentService.loadComment(dto);
    }
}
