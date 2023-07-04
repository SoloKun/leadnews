package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.aliyun.scan.GreenTextScan;
import com.heima.comment.service.CommentHotService;
import com.heima.comment.service.CommentService;
import com.heima.common.exception.CustException;
import com.heima.feigns.AdminFeign;
import com.heima.feigns.ArticleFeign;
import com.heima.feigns.UserFeign;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.comment.dtos.CommentDTO;
import com.heima.model.comment.dtos.CommentLikeDTO;
import com.heima.model.comment.dtos.CommentSaveDTO;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.vos.ApCommentVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.constants.article.HotArticleConstants;
import com.heima.model.mess.app.NewBehaviorDTO;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.SensitiveWordUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: CommentServiceImpl
 * Package: com.heima.comment.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/30 23:25
 * @Version 1.0
 */

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private AdminFeign adminFeign;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private ArticleFeign articleFeign;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    private CommentHotService commentHotService;
    @Autowired
    RedissonClient redisClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    public ResponseResult saveComment(CommentSaveDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "请先登录");
        }
        Integer userId = user.getId();
        if (userId == 0) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "请先登录");
        }
        ResponseResult<List<String>> lists = adminFeign.selectAllSensitives();
        if (!lists.checkCode()) {
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程获取敏感词失败");
        }
        SensitiveWordUtil.initMap(lists.getData());
        Map<String, Integer> map = SensitiveWordUtil.matchWords(dto.getContent());
        if (map != null && map.size() > 0) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "评论内容包含敏感词" + map.keySet());
        }
        String content = dto.getContent();
        try {
            Map resultMap = greenTextScan.greenTextScan(content);
            String suggestion = (String) resultMap.get("suggestion");
            if ("block".equals(suggestion)) {
                content = (String) resultMap.get("filteredContent");
            }


        } catch (Exception e) {
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用失败");
        }
        if (StringUtils.isEmpty(content)) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "评论内容不合法");
        }
        ResponseResult<ApUser> apUserResult = userFeign.findUserById(userId);
        if (!apUserResult.checkCode()) {
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用失败");
        }
        ResponseResult<ApArticle> apArticleResult = articleFeign.findById(dto.getArticleId());
        if (!apArticleResult.checkCode()) {
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用失败");
        }
        ApUser wholeUser = apUserResult.getData();
        //这里获取的是当前用户的信息，但是这里的信息不全，只有id，没有其他的信息
        //因为这里的user是从数据库中查询出来的，而AppThreadLocalUtils.getUser()是filter中设置的,
        //filter中只设置了id，没有设置其他的信息，所以这里需要重新查询一次
        ApArticle apArticle = apArticleResult.getData();
        ApComment apComment = new ApComment();
        apComment.setAuthorId(userId);
        apComment.setAuthorName(wholeUser.getName());
        apComment.setContent(content);
        apComment.setArticleId(dto.getArticleId());
        apComment.setImage(wholeUser.getImage());
        apComment.setFlag((short) 0);
        apComment.setLikes(0);
        apComment.setReply(0);
        apComment.setType((short) 0);
        apComment.setChannelId(apArticle.getChannelId());
        apComment.setLongitude(null);
        apComment.setLatitude(null);
        apComment.setAddress(null);
        apComment.setOrd(null);
        apComment.setCreatedTime(new Date());
        apComment.setUpdatedTime(new Date());
        mongoTemplate.insert(apComment);
        //发送 HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE
        NewBehaviorDTO newBehaviorDTO = new NewBehaviorDTO();
        newBehaviorDTO.setArticleId(dto.getArticleId());
        newBehaviorDTO.setType(NewBehaviorDTO.BehaviorType.COMMENT);
        newBehaviorDTO.setAdd(1);
        rabbitTemplate.convertAndSend(HotArticleConstants.HOT_ARTICLE_SCORE_BEHAVIOR_QUEUE,
                JSON.toJSONString(newBehaviorDTO));


        return ResponseResult.okResult();


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult like(CommentLikeDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "请先登录");
        }
        Integer userId = user.getId();

        String commentId = dto.getCommentId();
        Query query = Query.query(Criteria.where("id").is(commentId));
        Map<String, Object> map = new HashMap<>(1);
        //异步 引入redis分布式锁
        RLock lock = redisClient.getLock("likes-comment");
        lock.lock();
        Integer lastLikes = 0;
        try {
            ApComment apComment = mongoTemplate.findOne(query, ApComment.class);
            if (apComment == null) {
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "评论不存在");
            }


            lastLikes = apComment.getLikes();

            ApCommentLike apCommentLike = mongoTemplate.findOne(Query.query(Criteria.where("commentId").is(commentId)
                    .and("authorId").is(userId)), ApCommentLike.class);
            Short operation = dto.getOperation();

            if (operation.intValue() == 0) {
                // 判断是否重复点赞
                if (apCommentLike != null) {
                    CustException.cust(AppHttpCodeEnum.DATA_EXIST, "不能重复点赞");
                }
                apCommentLike = new ApCommentLike();
                apCommentLike.setCommentId(commentId);
                apCommentLike.setOperation(operation);
                apCommentLike.setAuthorId(userId);
                mongoTemplate.insert(apCommentLike);
                apComment.setLikes(lastLikes + 1);
                mongoTemplate.save(apComment);
                //todo 点赞>10 作为热点评论
                commentHotService.hotCommentExecutor(apComment);
            } else {
                if (apCommentLike == null) {
                    CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "不能重复取消点赞");
                }
                mongoTemplate.remove(apCommentLike);
                Integer like = apComment.getLikes();
                if (like > 1) {
                    apComment.setLikes(like - 1);
                } else {
                    apComment.setLikes(0);
                }
                mongoTemplate.save(apComment);
                lastLikes = apComment.getLikes();
            }
        } finally {
            lock.unlock();
        }

        map.put("likes", lastLikes);
        return ResponseResult.okResult(map);

    }

    @Override
    public ResponseResult loadComment(CommentDTO dto) {
        List<ApComment> commentList = new ArrayList<>();
        if (dto.getSize() == null || dto.getSize() == 0) {
            dto.setSize(10);
        }
        long articleId = dto.getArticleId();
        Date minDate = dto.getMinDate();
        Short index = dto.getIndex();
        if (index.intValue() == 1) {
            //查询热门评论
            Query query = Query.query(Criteria.where("articleId").is(articleId)
                            .and("flag").is((short) 1)
                            .and("updatedTime").lt(minDate))
                    .with(Sort.by(Sort.Direction.DESC, "likes"))
                    .with(Sort.by(Sort.Direction.DESC, "updatedTime"));
            List<ApComment> hotCommentList = mongoTemplate.find(query, ApComment.class);
            if (hotCommentList != null && hotCommentList.size() > 5) {
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "热评数量超过5条");
            }
            dto.setSize(dto.getSize() - hotCommentList.size());

            Query query1 = Query.query(Criteria.where("articleId").is(articleId)
                            .and("flag").is((short) 0)
                            .and("updatedTime").lt(minDate))
                    .with(Sort.by(Sort.Direction.DESC, "likes"))
                    .with(Sort.by(Sort.Direction.DESC, "updatedTime"));

            Pageable pageable = PageRequest.of(0, dto.getSize());
            query1.with(pageable);
            List<ApComment> commonComments = mongoTemplate.find(query1, ApComment.class);
            commentList.addAll(hotCommentList);
            commentList.addAll(commonComments);
        } else {
            //查询更多评论
            Query query1 = Query.query(Criteria.where("articleId").is(articleId)
                            .and("flag").is((short) 0)
                            .and("update_time").lt(minDate))
                    .with(Sort.by(Sort.Direction.DESC, "likes"))
                    .with(Sort.by(Sort.Direction.DESC, "updatedTime"));

            Pageable pageable = PageRequest.of(index - 1, dto.getSize());
            query1.with(pageable);
            List<ApComment> commonComments = mongoTemplate.find(query1, ApComment.class);
            commentList.addAll(commonComments);
        }

        ApUser user = AppThreadLocalUtils.getUser();
        //未登录直接返回
        if (user == null) {
            return ResponseResult.okResult(commentList);
        }
        //登录了，查询用户是否点赞
        List<ApCommentVo> commentVoList = new ArrayList<>();
        List<String> commentIds = commentList.stream()
                .map(ApComment::getId)
                .collect(Collectors.toList());
        List<ApCommentLike> apCommentLikes = mongoTemplate.find(
                Query.query(Criteria.where("commentId").in(commentIds)
                        .and("authorId").is(user.getId())), ApCommentLike.class);
        commentList.forEach(
                apComment -> {
                    ApCommentVo apCommentVo = new ApCommentVo();
                    BeanUtils.copyProperties(apComment, apCommentVo);
                    if (apCommentLikes.size() > 0) {
                        for (ApCommentLike apCommentLike : apCommentLikes) {
                            if (apCommentLike.getCommentId().equals(apComment.getId())) {
                                apCommentVo.setOperation((short) 0);
                                break;
                            }
                        }
                    }
                    commentVoList.add(apCommentVo);
                }
        );
        return ResponseResult.okResult(commentVoList);
    }
}
