package com.heima.model.comment.dtos;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

@Data
public class CommentRepayLikeDTO {
    /**
     * 回复id
     */
    private String commentRepayId;
    /**
     * 0：点赞
     * 1：取消点赞
     */
    @NotNull(message = "操作类型不可以为空")
    @Range(min = 0,max = 1,message = "操作类型不合法")
    private Short operation;
}