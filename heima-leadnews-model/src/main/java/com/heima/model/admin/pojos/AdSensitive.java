package com.heima.model.admin.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ClassName: AdSenstitive
 * Package: com.heima.model.admin.poji
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 19:44
 * @Version 1.0
 */
@Data
@TableName("ad_sensitive")
public class AdSensitive implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type= IdType.AUTO)
    private Integer id;
    @TableField("sensitives")
    //tableField注解用于实体类属性和数据库表字段的映射和tableId注解类似，
    // 区别在于tableId注解只能标注在主键属性上，而tableField注解可以标注在任意属性上
    private String sensitives;
    @TableField("created_time")
    private Date createdTime;



}
