package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: WmMaterialMapper
 * Package: com.heima.wemedia.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 0:30
 * @Version 1.0
 */
@Repository
public interface WmMaterialMapper extends BaseMapper<WmMaterial> {
    /**
     * 根据素材资源路径，查询相关素材id
     * @param urls 素材路径
     * @param userId
     * @return
     */
    public List<Integer> selectRelationsIds(@Param("urls") List<String> urls,
                                            @Param("userId") Integer userId);
}
