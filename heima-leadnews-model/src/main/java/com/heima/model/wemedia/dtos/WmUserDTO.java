package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * ClassName: WmUserDTO
 * Package: com.heima.model.wemedia.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/17 20:20
 * @Version 1.0
 */
@Data
public class WmUserDTO {
    /**
     * 用户名
     */
    private String name;
    /**
     * 密码
     */
    private String password;
}
