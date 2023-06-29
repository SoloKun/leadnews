package com.heima.model.behavior.dtos;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NotNull(message = "传输的实体不能为null")
public class ReadBehaviorDTO {
    // 设备ID
    @NotNull(message = "设备id不能为空")
    Integer equipmentId;
    // 文章、动态、评论等ID
    @NotNull(message = "文章id不能为空")
    @Min(value = 1, message = "关联文章id错误")
    Long articleId;
    /**
     * 阅读次数  
     */

    Short count;
}