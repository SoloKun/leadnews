package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * ClassName: ApArticleContent
 * Package: com.heima.model.article.pojos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/21 14:08
 * @Version 1.0
 */
@Data
@TableName("ap_article_content")
public class ApArticleContent {
    @TableId(value = "id",type = IdType.ID_WORKER)
    private Long id;

    /**
     * 文章id
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 评论内容
     */
    private String content;
}
