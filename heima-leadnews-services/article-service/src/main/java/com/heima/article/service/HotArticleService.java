package com.heima.article.service;

import com.heima.model.mess.app.AggBehaviorDTO;

public interface HotArticleService{
    /**
     * 计算热文章
     */
    public void computeHotArticle();

    public void updateApArticle(AggBehaviorDTO aggBehavior);


}