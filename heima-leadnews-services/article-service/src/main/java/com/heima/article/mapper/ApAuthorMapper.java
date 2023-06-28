package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.pojos.ApAuthor;
import org.apache.ibatis.annotations.Mapper;

/**
 * ClassName: AuthorMapper
 * Package: com.heima.article.mapper
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 16:47
 * @Version 1.0
 */
@Mapper
public interface ApAuthorMapper extends BaseMapper<ApAuthor> {
}
