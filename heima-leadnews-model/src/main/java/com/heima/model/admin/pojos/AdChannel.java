package com.heima.model.admin.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: AdChannel
 * Package: com.heima.model.admin.poji
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/13 21:03
 * @Version 1.0
 */
@Data
@TableName("ad_channel")
public class AdChannel implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private  Integer id;
    /**
     * 频道名称
     */
    @TableField("name")
    private String name;
    /**
     * 频道描述
     */
    @TableField("description")
    private String description;
    /**
     * 是否默认频道
     */
    @TableField("is_default")
    private Boolean isDefault;
    @TableField("status")
    private Boolean status;
    /**
     * 默认排序
     */
    @TableField("ord")
    private Integer ord;
    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;
}
