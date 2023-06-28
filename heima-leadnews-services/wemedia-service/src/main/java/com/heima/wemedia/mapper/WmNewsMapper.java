package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.dtos.NewsAuthDTO;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.vos.WmNewsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ClassName: WmNewsService
 * Package: com.heima.wemedia.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/18 23:30
 * @Version 1.0
 */
@Repository
public interface WmNewsMapper extends BaseMapper<WmNews> {
    List<WmNewsVO> findListAndPage(@Param("dto") NewsAuthDTO dto);
    long findListCount(@Param("dto") NewsAuthDTO dto);
}
