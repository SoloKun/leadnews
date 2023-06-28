package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDTO;
import lombok.Data;

/**
 * ClassName: WmMaterialDTO
 * Package: com.heima.model.wemedia.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 14:21
 * @Version 1.0
 */
@Data
public class WmMaterialDTO extends PageRequestDTO {
    Short isCollection; //1 查询收藏的   0: 未收藏
}
