package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleHomeDTO;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;


public interface ApArticleService extends IService<ApArticle> {
    public void publishArticle(Integer newsId);
    public ResponseResult load(Short loadtype, ArticleHomeDTO dto);

    public ResponseResult load2(Short loadtypeLoadMore, ArticleHomeDTO dto,boolean firstPage);

    public ResponseResult getById(Integer articleId);
}