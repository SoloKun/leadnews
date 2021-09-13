package com.heima.model.comment.dtos;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data
public class CommentSaveDTO {
    /**
     * 文章id
     */
    @NotNull(message = "文章id不能为空")
    private Long articleId;
    /**
     * 评论内容
     */
    @NotBlank(message = "文章内容不能为空")
    @Length(max = 140,message = "评论能不能大于140个字符")
    private String content;
}