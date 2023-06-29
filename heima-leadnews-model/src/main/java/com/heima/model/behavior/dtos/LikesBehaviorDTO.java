package com.heima.model.behavior.dtos;

import com.heima.model.common.validator.ValidatorAddGroup;
import com.heima.model.common.validator.ValidatorDeleteGroup;
import com.heima.model.common.validator.ValidatorUpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(groups = {ValidatorAddGroup.class, ValidatorDeleteGroup.class},message = "传输的实体不能为null")
public class LikesBehaviorDTO {
    // 设备ID
    Integer equipmentId;
    // 文章、动态、评论等ID
    @NotNull(message = "文章id不能为空",groups = {ValidatorAddGroup.class, ValidatorUpdateGroup.class})
    @Min(value = 1, message = "关联文章id错误", groups = {ValidatorAddGroup.class, ValidatorUpdateGroup.class})
    Long articleId;
    /**
     * 喜欢内容类型
     * 0文章
     * 1动态
     * 2评论
     */
    @NotNull(message = "点赞内容类型不能为空", groups = {ValidatorAddGroup.class, ValidatorUpdateGroup.class})
    @Range(min = 0, max = 2, message = "内容类型不符合")
    Short type;
    /**
     * 喜欢操作方式
     * 0 点赞
     * 1 取消点赞
     */
    @NotNull(message = "操作类型不能为空", groups = {ValidatorUpdateGroup.class, ValidatorAddGroup.class})
    @Range(min = 0, max = 1, message = "操作类型不符合")
    Short operation;
}