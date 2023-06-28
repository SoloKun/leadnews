package com.heima.article;

import com.heima.article.service.ApArticleService;
import com.heima.article.service.impl.ApArticleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ClassName: articleTest
 * Package: com.heima
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/21 15:32
 * @Version 1.0
 */
@SpringBootTest
public class articleTest {
    @Autowired
    ApArticleServiceImpl apArticleService;
    @Test
    public void publishArticle(){
        apArticleService.publishArticle(6266);
    }
}
