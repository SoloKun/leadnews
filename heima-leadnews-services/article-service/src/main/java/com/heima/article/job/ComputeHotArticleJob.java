package com.heima.article.job;

import com.heima.article.service.HotArticleService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: ComputeHotArticleJob
 * Package: com.heima.article.job
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/3 16:23
 * @Version 1.0
 */
@Component
@Slf4j
public class ComputeHotArticleJob {
    @Autowired
    private HotArticleService hotArticleService;
    @XxlJob("computeHotArticleJob")
    public ReturnT computeHotArticle(String param) throws Exception {
        log.info("开始计算热文章");
        //todo 计算热文章
        hotArticleService.computeHotArticle();
        log.info("计算热文章完成");
        return ReturnT.SUCCESS;
    }

}
