package com.heima.feigns.fallback;

import com.heima.feigns.ArticleFeign;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVO;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: ArticleFeignFallback
 * Package: com.heima.feigns.fallback
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 18:18
 * @Version 1.0
 */
@Component
@Slf4j
public class ArticleFeignFallback implements FallbackFactory<ArticleFeign> {


    @Override
    public ArticleFeign create(Throwable throwable) {
        throwable.printStackTrace();
        return new ArticleFeign() {
            @Override
            public ResponseResult<ApAuthor> findByUserId(Integer userId) {
                log.error("参数: {}",userId);
                log.error("ArticleFeign findByUserId 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }

            @Override
            public ResponseResult<ApAuthor> save(ApAuthor apAuthor) {
                log.error("参数: {}",apAuthor);
                log.error("ArticleFeign save 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }

            @Override
            public ResponseResult<ApArticle> findById(Long id) {
                log.error("参数: {}",id);
                log.error("ArticleFeign save 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }

            @Override
            public ResponseResult<SearchArticleVO> findArticle(Long id) {
                log.error("参数: {}",id);
                log.error("ArticleFeign findArticle 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }
        };
    }
}
