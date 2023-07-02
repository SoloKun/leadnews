package com.heima.comment.service.impl;

import com.heima.comment.service.CommentHotService;
import com.heima.model.comment.pojos.ApComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: CommentHotServiceImpl
 * Package: com.heima.comment.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/2 13:54
 * @Version 1.0
 */
@Slf4j
@Service
public class CommentHotServiceImpl implements CommentHotService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    @Async("taskExecutor")
    public void hotCommentExecutor(ApComment apComment) {
        log.info("异步计算热点文章==================> 开始");
        // 1. 查询当前文章下的所有热点评论集合
        //     1.1 按照文章id   flag=1(热点文章)   点赞降序
        long articleId = apComment.getArticleId();
        Query query = Query.query(Criteria.where("articleId").is(articleId)
                .and("flag").is(1)).with(Sort.by(Sort.Order.desc("likes")));
        List<ApComment>hotComments = mongoTemplate.find(query, ApComment.class);
        // 2. 如果 热评集合为空  或  热评数量小于5 直接将当前评论改为热评
        if(hotComments==null||hotComments.size()<5){
            apComment.setFlag((short)1);
            mongoTemplate.save(apComment);
            return;
        }else{// 3. 如果热评数量大于等于 5
            // 3.1  获取热评集合中 最后点赞数量最少的热评
            ApComment lastHotComment = hotComments.get(hotComments.size() - 1);
            // 3.2 和当前评论点赞数量做对比  谁的点赞数量多 改为热评
            if(lastHotComment.getLikes()<apComment.getLikes()){
                // 3.3 将当前评论改为热评
                apComment.setFlag((short)1);
                mongoTemplate.save(apComment);
                // 3.4 将最后点赞数量最少的热评改为普通评论
                lastHotComment.setFlag((short)0);
                mongoTemplate.save(lastHotComment);
            }
        }


        log.info("异步计算热点文章==================> 结束");
    }
}
