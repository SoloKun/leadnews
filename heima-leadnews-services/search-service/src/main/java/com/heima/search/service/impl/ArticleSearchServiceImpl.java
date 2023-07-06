package com.heima.search.service.impl;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.heima.common.exception.CustException;
import com.heima.es.service.EsService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.search.SearchConstants;
import com.heima.model.search.dtos.UserSearchDTO;
import com.heima.model.search.vos.SearchArticleVO;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * ClassName: ArticleSearchServiceImpl
 * Package: com.heima.search.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/5 17:09
 * @Version 1.0
 */
@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {
    @Autowired
    EsService esService;
    @Value("${file.minio.readPath}")
    String readPath;
    @Value("${file.oss.web-site}")
    String webSite;
    @Autowired
    private ApUserSearchService apUserSearchService;
    @Override
    public ResponseResult search(UserSearchDTO userSearchDto) {
        //1.参数校验
        String searchWords = userSearchDto.getSearchWords();
        if (StringUtils.isBlank(searchWords)) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "搜索关键字不能为空");
        }
        ApUser user = AppThreadLocalUtils.getUser();
        if(user !=null) userSearchDto.setLoginUserId(user.getId());
        // 异步调用保存用户输入关键词记录
        apUserSearchService.insert(userSearchDto);


        //2.构建搜索请求对象 SearchSourceBuilder
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //3.设置搜索条件
        //3.1 创建bool查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //3.2 关键词查询
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("title", searchWords);
        boolQueryBuilder.must(queryBuilder);
        if(userSearchDto.getMinBehotTime()==null){
            userSearchDto.setMinBehotTime(new Date());
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime").lte(userSearchDto.getMinBehotTime().getTime());
        boolQueryBuilder.filter(rangeQueryBuilder);
        //分页
        builder.from(0);
        builder.size(userSearchDto.getPageSize());
        //按照时间倒序
        builder.sort("publishTime", SortOrder.DESC);
        //4.设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color:red; font-size:inherit;'>");
        highlightBuilder.postTags("</font>");
        builder.highlighter(highlightBuilder);
        builder.query(boolQueryBuilder);
        //5.执行搜索
        PageResponseResult searchResult = esService.search(builder,
                SearchArticleVO.class, SearchConstants.ARTICLE_INDEX_NAME);
        List<SearchArticleVO> list = (List<SearchArticleVO>) searchResult.getData();
        if (!CollectionUtils.isEmpty(list)) {
            for (SearchArticleVO searchArticleVO : list) {
                searchArticleVO.setStaticUrl(readPath + searchArticleVO.getStaticUrl());
                String images = searchArticleVO.getImages();
                if (StringUtils.isNotBlank(images)) {
                    String[] split = images.split(",");
                    searchArticleVO.setImages(webSite + split[0]);
                }

            }
        }



        return searchResult;
    }

    @Override
    public void saveArticle(SearchArticleVO article) {
        esService.save(article,SearchConstants.ARTICLE_INDEX_NAME);
    }
    @Override
    public void deleteArticle(String articleId) {

        esService.deleteById(articleId,SearchConstants.ARTICLE_INDEX_NAME);
    }
}
