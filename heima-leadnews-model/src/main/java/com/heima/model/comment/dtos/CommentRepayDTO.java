package com.heima.model.comment.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NotNull(message = "参数不可以为空")
public class CommentRepayDTO {
    /**
     * 评论id
     */
    private String commentId;
    private Integer size;
    // 最小时间
    private Date minDate;
}