package com.heima.model.comment.dtos;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
@NotNull(message = "参数不可以为空")
public class CommentSaveDTO {
    /**
     * 文章id
     */
    @NotNull(message = "文章id不可以为空")
    @Min(value = 1,message = "文章id不合法")
    private Long articleId;
    /**
     * 评论内容
     */
    @Length(max = 140,message = "评论内容不可以超过140个字")
    private String content;
}