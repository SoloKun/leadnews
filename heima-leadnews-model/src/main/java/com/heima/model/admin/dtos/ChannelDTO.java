package com.heima.model.admin.dtos;

import com.heima.model.common.dtos.PageRequestDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ClassName: ChannelDTO
 * Package: com.heima.model.admin.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/13 21:25
 * @Version 1.0
 */

@Data
public class ChannelDTO extends PageRequestDTO {
    /**
     * 频道名称
     */
    @ApiModelProperty("频道名称")
    private String name;
    /**
     * 频道状态
     */
    @ApiModelProperty("频道状态 0 关闭 1 开启")
    private  Integer status;
}
