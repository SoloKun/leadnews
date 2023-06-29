package com.heima.model.behavior.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "传输的实体不能为null")
public class ArticleBehaviorDTO {
    // 设备ID
    @NotNull(message = "传输的实体不能为null")
    Integer equipmentId;
    // 文章ID 
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "文章id不能为空")
    @Min(value = 1,message = "关联文章id错误")
    Long articleId;
    // 作者ID
    @NotNull(message = "作者id不能为空")
    @Min(value = 1,message = "作者id错误")
    Integer authorId;
    // 作者对应的apuserid
    @NotNull(message = "作者apuserid不能为空")
    @Min(value = 1,message = "作者apuserid错误")
    Integer authorApUserId;
}