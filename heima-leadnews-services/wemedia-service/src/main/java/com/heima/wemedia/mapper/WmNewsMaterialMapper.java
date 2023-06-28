package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: wmNewsMaterialMapper
 * Package: com.heima.wemedia.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 14:52
 * @Version 1.0
 */
@Repository
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {
    /**
     * 保存素材与文章的关联关系
     * @param wmMaterialIds
     * @param newsId
     * @param type
     */
    public void saveRelations(@Param("wmMaterialIds") List<Integer> wmMaterialIds,
                              @Param("newsId") Integer newsId,
                              @Param("type") Short type);
}
