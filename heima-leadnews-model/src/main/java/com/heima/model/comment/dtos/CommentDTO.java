package com.heima.model.comment.dtos;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@NotNull(message = "参数不可以为空")
public class CommentDTO {
    @NotNull(message = "文章id不可以为空")
    private Long articleId;
    // 最小时间
    private Date minDate;
    //是否是首页

    private Short index;
    // 每页条数

    private Integer size;
}