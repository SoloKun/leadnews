package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApAuthorMapper;
import com.heima.article.service.GeneratePageService;
import com.heima.common.exception.CustException;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.enums.AppHttpCodeEnum;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: GeneratePageServiceImpl
 * Package: com.heima.article.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 19:01
 * @Version 1.0
 */
@Slf4j
@Service
public class GeneratePageServiceImpl implements GeneratePageService {
    @Autowired
    private Configuration configuration;
    @Autowired
    private ApAuthorMapper  apAuthorMapper;
    @Autowired
    @Qualifier("minIOFileStorageService")
    private FileStorageService fileStorageService;
    @Value("${file.minio.prefix}")
    private String prefix;
    @Value("${file.minio.readPath}")
    private String readPath;
    @Autowired
    ApArticleMapper apArticleMapper;
    @Override
    public void generateArticlePage(String content, ApArticle apArticle) {

        //获取freemarker模板
        try {
            Template template = configuration.getTemplate("article.ftl");
            Map params = new HashMap<>();
            params.put("article",apArticle);
            params.put("content", JSON.parseArray(content,Map.class));
            ApAuthor apAuthor = apAuthorMapper.selectById(apArticle.getAuthorId());
            if (apAuthor==null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"作者不存在");
            }
            params.put("authorApUserId",apAuthor.getUserId());
            StringWriter out = new StringWriter();
            template.process(params,out);
            String htmlStr = out.toString();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(htmlStr.getBytes());
            String path = fileStorageService.store(prefix,apArticle.getId()+".html","text/html",byteArrayInputStream);
            apArticle.setStaticUrl(path);
            apArticleMapper.updateById(apArticle);
            log.info("生成静态页成功",readPath+path);

        } catch (IOException e) {
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR,"获取模板失败");
        } catch (TemplateException e) {
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR,"模板解析失败");
        }

        //生成静态页




    }
}
