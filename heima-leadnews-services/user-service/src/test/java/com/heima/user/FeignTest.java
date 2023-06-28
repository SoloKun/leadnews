package com.heima.user;

import com.heima.feigns.ArticleFeign;
import com.heima.feigns.WemediaFeign;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.pojos.WmUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

/**
 * ClassName: FeignTest
 * Package: com.heima.user
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 16:04
 * @Version 1.0
 */
@SpringBootTest
public class FeignTest {
    @Autowired
    private WemediaFeign wemediaFeign;
    @Test
    public void testwemedia(){

        ResponseResult<WmUser> zhangshan = wemediaFeign.findByName("zhangsan");
        System.out.println(zhangshan.getData());
        zhangshan.getData().setName("zhangsan2");
        zhangshan.getData().setId(null);
        // WmUser wmUser = new ApUser(salt=123, name=suwukong, password=suwukong, phone=13511223458, image=null, sex=true, certification=null, identityAuthentication=null, status=true, flag=1, createdTime=Sat Aug 01 11:09:57 CST 2020)
        WmUser wmUser = new WmUser();
        wmUser.setName("kkkk");
        ResponseResult<WmUser> save = wemediaFeign.save(wmUser);
        System.out.println(save.getData());
    }
    @Autowired
    private ArticleFeign articleFeign;
    @Test
    public void testArticle(){
        ResponseResult<ApAuthor>res = articleFeign.findByUserId(4);
        System.out.println(res.getData());
        ApAuthor newapAuthor = new ApAuthor();
        newapAuthor.setName("zhangsan1");
        newapAuthor.setUserId(10);
        newapAuthor.setType(2);
        newapAuthor.setCreatedTime(new Date());
        ResponseResult<ApAuthor> save = articleFeign.save(newapAuthor);
        System.out.println(save.getData());
    }
}
