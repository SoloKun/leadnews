package com.heima.model.behavior.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "传输的实体不能为null")
public class CollectionBehaviorDTO {
    // 设备ID
    @NotNull(message = "设备id不能为空")
    Integer equipmentId;
    // 文章、动态ID
    @JsonAlias("entryId") // 前端变量命名entryId 实际为articleId 因此起个别名
    @NotNull(message = "文章id不能为空")
    @Min(value = 1,message = "关联文章id错误")
    Long articleId;
    /**
     * 收藏内容类型
     * 0文章
     * 1动态
     */
    @NotNull(message = "收藏类型不能为空")
    @Range(min = 0,max = 1,message = "收藏类型不符合")
    Short type;
    /**
     * 操作类型
     * 0收藏
     * 1取消收藏
     */
    @NotNull(message = "操作类型不能为空")
    @Range(min = 0,max = 1,message = "操作类型不符合")
    Short operation;
}