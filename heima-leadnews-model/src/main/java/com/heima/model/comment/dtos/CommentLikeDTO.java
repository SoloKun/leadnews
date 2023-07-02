package com.heima.model.comment.dtos;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "参数不可以为空")
public class CommentLikeDTO {

    /**
     * 评论id
     */
    @NotNull(message = "评论id不可以为空")
    private String commentId;

    /**
     * 0：点赞
     * 1：取消点赞
     */
    @NotNull(message = "操作类型不可以为空")
    @Range(min = 0,max = 1,message = "操作类型不合法")
    private Short operation;
}