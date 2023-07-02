package com.heima.comment.controller.v1;

import com.heima.comment.service.CommentRepayService;
import com.heima.model.comment.dtos.CommentRepayDTO;
import com.heima.model.comment.dtos.CommentRepayLikeDTO;
import com.heima.model.comment.dtos.CommentRepaySaveDTO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.validator.ValidatorUpdateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment_repay")
@Api(value = "App回复评论模块", tags = "App回复评论")
public class CommentRepayController {
    @Autowired
    private CommentRepayService commentRepayService;

    @PostMapping("/save")
    @ApiOperation("发表回复评论")
    public ResponseResult saveComment(@RequestBody @Validated CommentRepaySaveDTO commentRepaySaveDTO) {
        return commentRepayService.saveCommentRepay(commentRepaySaveDTO);
    }

    @PostMapping("/load")
    @ApiOperation("加载回复评论")
    public ResponseResult loadComment(@RequestBody @Validated CommentRepayDTO dto) {
        return commentRepayService.loadCommentRepay(dto);
    }
    @PostMapping("/like")
    @ApiOperation("点赞回复评论")
    public ResponseResult likeComment(@RequestBody @Validated CommentRepayLikeDTO dto) {
        return commentRepayService.saveCommentRepayLike(dto);
    }
}
