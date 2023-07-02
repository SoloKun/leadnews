package com.heima.feigns;

import com.heima.config.HeimaFeignAutoConfiguration;
import com.heima.feigns.fallback.ArticleFeignFallback;
import com.heima.feigns.fallback.WemediaFeignFallback;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ClassName: ArticleFeign
 * Package: com.heima.feigns
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 18:17
 * @Version 1.0
 */
@FeignClient(
        value = "leadnews-article",
        fallbackFactory = ArticleFeignFallback.class,
        configuration = HeimaFeignAutoConfiguration.class
)
public interface ArticleFeign {
    @GetMapping("/api/v1/author/findByUserId/{userId}")
    public ResponseResult<ApAuthor> findByUserId(@PathVariable("userId") Integer userId);
    @PostMapping("/api/v1/author/save")
    public ResponseResult<ApAuthor> save(@RequestBody ApAuthor apAuthor);

    @GetMapping("/api/v1/article/findById/{id}")
    public ResponseResult<ApArticle> findById(@PathVariable("id") Long id);


}
