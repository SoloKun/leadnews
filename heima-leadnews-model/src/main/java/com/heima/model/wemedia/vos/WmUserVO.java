package com.heima.model.wemedia.vos;

import lombok.Data;

import java.util.Date;

/**
 * ClassName: WmUserVO
 * Package: com.heima.model.wemedia.vos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/17 20:22
 * @Version 1.0
 */
@Data
public class WmUserVO {
    private Integer id;
    private String name;
    private String nickname;
    private String image;
    private String email;
    private Date loginTime;
    private Date createdTime;
}

