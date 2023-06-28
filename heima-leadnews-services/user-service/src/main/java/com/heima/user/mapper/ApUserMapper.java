package com.heima.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.user.pojos.ApUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: ApUserMapper
 * Package: com.heima.user.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 18:43
 * @Version 1.0
 */
@Mapper
public interface ApUserMapper extends BaseMapper<ApUser> {
}
