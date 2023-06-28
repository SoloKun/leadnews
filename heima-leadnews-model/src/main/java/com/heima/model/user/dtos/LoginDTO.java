package com.heima.model.user.dtos;

import lombok.Data;

/**
 * ClassName: LoginDTO
 * Package: com.heima.model.user.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 15:51
 * @Version 1.0
 */
@Data
public class LoginDTO  {

    /**
     * 设备id
     */
    private Integer equipmentId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;
}