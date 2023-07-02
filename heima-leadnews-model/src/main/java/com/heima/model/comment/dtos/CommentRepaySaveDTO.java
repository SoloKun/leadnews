package com.heima.model.comment.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "参数不可以为空")
public class CommentRepaySaveDTO {
    /**
     * 评论id
     */
    @NotNull(message = "评论id不可以为空")
    private String commentId;
    /**
     * 回复内容
     */
    @NotNull(message = "回复内容不可以为空")
    private String content;
}