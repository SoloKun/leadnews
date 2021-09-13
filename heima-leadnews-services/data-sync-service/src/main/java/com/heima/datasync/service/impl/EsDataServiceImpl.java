package com.heima.datasync.service.impl;
import com.alibaba.fastjson.JSON;
import com.heima.datasync.mapper.ApArticleMapper;
import com.heima.datasync.service.EsDataService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
@Service
public class EsDataServiceImpl implements EsDataService {
    public static final String ARTICLE_INDEX_NAME = "app_info_article";
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Override
    public ResponseResult dataInit() {
        // TODO 判断索引库是否存在
        // 批量导入数据:
        // 一般建议是1000-5000个文档，如果你的文档很大，可以适当减少队列，大小建议是5-15MB，默认不能超过100M
        // TODO 如果数据库数据过多 建议分段批量插入
        try {
            // 分页查询第一页文章数据
            List<ApArticle> apArticles = apArticleMapper.findAllArticles();
            if(apArticles == null || apArticles.size() == 0){
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "数据库文章信息不存在");
            }
            // 遍历页码批量插入到索引库
            BulkRequest bulkRequest = new BulkRequest(ARTICLE_INDEX_NAME);
            for (ApArticle record : apArticles) {
                // 封装数据
                SearchArticleVo articleVo = new SearchArticleVo();
                BeanUtils.copyProperties(record, articleVo);
                // 转json
                String articleJson = JSON.toJSONString(articleVo);
                // 设置文档ID
                IndexRequest indexRequest = new IndexRequest()
                        .id(record.getId().toString())
                        .source(articleJson, XContentType.JSON);
                // 添加到批量插入对象
                bulkRequest.add(indexRequest);
            }
            BulkResponse responses = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            return ResponseResult.okResult(responses.status());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, e.getMessage());
        }
    }
}