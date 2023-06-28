package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApAuthorMapper;
import com.heima.article.service.AuthorService;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * ClassName: AuthorServiceImpl
 * Package: com.heima.article.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 17:12
 * @Version 1.0
 */
@Service
public class AuthorServiceImpl extends ServiceImpl<ApAuthorMapper, ApAuthor> implements AuthorService {


    @Override
    public ResponseResult findByUserId(Integer userId) {
        ApAuthor apAuthor = getOne(Wrappers.<ApAuthor>lambdaQuery().eq(ApAuthor::getUserId, userId));
        return ResponseResult.okResult(apAuthor);
    }

    @Override
    public ResponseResult saveUser(ApAuthor apAuthor) {
        apAuthor.setCreatedTime(new Date());
        save(apAuthor);
        return ResponseResult.okResult(apAuthor);
    }

}
