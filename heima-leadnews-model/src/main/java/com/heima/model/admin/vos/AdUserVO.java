package com.heima.model.admin.vos;

import lombok.Data;

import java.util.Date;

/**
 * ClassName: AdUserVO
 * Package: com.heima.model.admin.vos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 22:13
 * @Version 1.0
 */
@Data
public class AdUserVO {
    private Integer id;
    private String name;
    private String nickname;
    private String image;
    private String email;
    private Date   loginTime;
    private Date createdTime;
}
