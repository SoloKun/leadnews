package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDTO;
import lombok.Data;

/**
 * ClassName: AuthDTO
 * Package: com.heima.model.user.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 23:00
 * @Version 1.0
 */
@Data
public class AuthDTO extends PageRequestDTO {
    //状态
    private Short status;
    //用户id
    private Integer id;
    //驳回信息
    private String msg;

}
