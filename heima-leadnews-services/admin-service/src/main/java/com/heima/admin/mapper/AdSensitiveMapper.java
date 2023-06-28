package com.heima.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.admin.pojos.AdSensitive;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: AdSensitiveMapper
 * Package: com.heima.admin.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 20:11
 * @Version 1.0
 */
@Repository
public interface AdSensitiveMapper extends BaseMapper<AdSensitive>{
    @Select("select sensitives from ad_sensitive")
    List<String> findAllSensitives();
}
