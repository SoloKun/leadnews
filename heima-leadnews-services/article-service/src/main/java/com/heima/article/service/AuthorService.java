package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;

/**
 * ClassName: AuthorService
 * Package: com.heima.article.service
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 17:06
 * @Version 1.0
 */
public interface AuthorService extends IService<ApAuthor> {
    ResponseResult findByUserId(Integer userId);


    ResponseResult saveUser(ApAuthor apAuthor);
}
