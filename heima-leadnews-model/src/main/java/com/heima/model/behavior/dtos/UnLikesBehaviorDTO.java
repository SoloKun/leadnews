package com.heima.model.behavior.dtos;

import lombok.Data;
import org.checkerframework.checker.units.qual.min;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "传输的实体不能为null")
public class UnLikesBehaviorDTO {
    // 设备ID
    @NotNull(message = "设备id不能为空")
    Integer equipmentId;
    // 文章ID
    @NotNull(message = "文章id不能为空")
    @Min(value = 1,message = "关联文章id错误")
    Long articleId;
    /**
     * 不喜欢操作方式
     * 0 不喜欢
     * 1 取消不喜欢
     */
    @NotNull(message = "操作类型不能为空")
    @Range(min = 0,max = 1,message = "操作类型不符合")
    Short type;

}