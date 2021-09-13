package com.heima.article;

import com.heima.article.service.ApArticleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ArticleTest {
    @Autowired
    ApArticleService apArticleService;
    @Test
    public void publishArticle(){
        apArticleService.publishArticle(6234);
    }
}
