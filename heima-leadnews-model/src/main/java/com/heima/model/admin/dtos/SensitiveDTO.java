package com.heima.model.admin.dtos;

import com.heima.model.common.dtos.PageRequestDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * ClassName: SensitiveDTO
 * Package: com.heima.model.admin.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 19:49
 * @Version 1.0
 */
@Data
public class SensitiveDTO extends PageRequestDTO {
    @ApiModelProperty("敏感词名称")
    private String name;
}
