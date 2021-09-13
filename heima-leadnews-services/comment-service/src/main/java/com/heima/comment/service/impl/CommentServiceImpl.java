package com.heima.comment.service.impl;
import com.heima.comment.service.CommentHotService;
import com.heima.comment.service.CommentService;
import com.heima.feigns.UserFeign;
import com.heima.model.comment.dtos.CommentDTO;
import com.heima.model.comment.dtos.CommentLikeDTO;
import com.heima.model.comment.dtos.CommentSaveDTO;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.vos.ApCommentVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private CommentHotService commentHotService;
    /**
     * 根据 文章id 查询评论列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findByArticleId(CommentDTO dto) {
        //1 参数检查
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        Integer size = dto.getSize();
        if ( size == null || size <= 0) {
            dto.setSize(10);
        }
        //2 查询Mongo文章所有评论列表
        List<ApComment> apCommentList = mongoTemplate.find(
                Query.query(Criteria.where("articleId").is(dto.getArticleId())
                        .and("createdTime").lt(dto.getMinDate()))
                        .limit(dto.getSize())
                        .with(Sort.by(Sort.Direction.DESC,"createdTime")),
                ApComment.class
        );
        //3 封装查询结果
        //3.1 用户未登录 直接返回评论列表
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.okResult(apCommentList);
        }

        //3.2 用户登录，需要加载当前用户对评论点赞的列表
        List<ApCommentVo> apCommentVoList = new ArrayList<>();
        // 获取文章对应的所有评论ID列表
        List<String> idList = apCommentList.stream().map(comment -> comment.getId()).collect(Collectors.toList());
        // 查询 点赞批量列表 按照评论id 筛选
        List<ApCommentLike> apCommentLikes = mongoTemplate.find(
                Query.query(Criteria.where("commentId").in(idList)
                        .and("authorId").is(user.getId()))
                , ApCommentLike.class);

        // 遍历当前用户点赞列表 和当前评论列表
        if(CollectionUtils.isNotEmpty(apCommentList)
                && CollectionUtils.isNotEmpty(apCommentLikes)){
            apCommentList.forEach(comment ->{ //遍历所有文章列表ID
                ApCommentVo apCommentVo = new ApCommentVo();
                BeanUtils.copyProperties(comment, apCommentVo);
                //遍历当前用户点赞列表
                apCommentLikes.forEach(apCommentLike ->{
                    // 文章评论列表id 和 当前用户点赞评论列表id是否相等
                    if (comment.getId().equals(apCommentLike.getCommentId())) {
                        apCommentVo.setOperation((short) 0);
                    }
                });
                apCommentVoList.add(apCommentVo);
            });
            return ResponseResult.okResult(apCommentVoList);
        }
        return ResponseResult.okResult(apCommentList);
    }
    /**
     * 点赞评论
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult like(CommentLikeDTO dto) {
        //1 参数检查 (Validated校验)

        //2 判断用户登录
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //3 查询评论
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class);
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 用户不能重复点赞
        ApCommentLike apCommentLike = mongoTemplate.findOne(
                Query.query(Criteria.where("authorId").is(user.getId())
                        .and("commentId").is(dto.getCommentId())
                ),
                ApCommentLike.class);
        Short operation = dto.getOperation();
        if (apCommentLike != null && operation.intValue() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "已点赞");
        }

        if (operation.intValue() == 0) { //点赞
            // 点赞+1
            apComment.setLikes(apComment.getLikes() + 1);
            mongoTemplate.save(apComment);

            // 保存评论点赞信息
            apCommentLike = new ApCommentLike();
            apCommentLike.setAuthorId(user.getId());
            apCommentLike.setCommentId(apComment.getId());
            apCommentLike.setOperation(dto.getOperation());
            mongoTemplate.save(apCommentLike);

            //计算热点评论评论
            if (apComment.getLikes() >= 10 && apComment.getFlag().shortValue() == 0) {
                commentHotService.hotCommentExecutor(apComment);
            }
        } else {
            // 取消点赞
            apComment.setLikes(
                    apComment.getLikes() <= 0 ? 0: apComment.getLikes() - 1);
            mongoTemplate.save(apComment);

            //删除点赞评论信息
            mongoTemplate.remove( Query.query( Criteria.
                    where("authorId").is(user.getId())
                    .and("commentId").is(apComment.getId()) ), ApCommentLike.class);
        }

        //4 数据返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("likes",apComment.getLikes());
        return ResponseResult.okResult(resultMap);
    }

    @Override
    public ResponseResult saveComment(CommentSaveDTO dto) {
        //1.检查参数 (validated校验)
        //2.判断是否登录
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //3.安全过滤,自行实现
        //4.保存评论
        ResponseResult<ApUser> apUserResult = userFeign.findUserById(user.getId());
        if(apUserResult.getCode()!=0 || apUserResult.getData() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE, "当前登录信息有误");
        }
        user = apUserResult.getData();
        ApComment apComment = new ApComment();
        apComment.setAuthorId(user.getId());
        apComment.setAuthorName(user.getName());
        apComment.setImage(user.getImage());
        apComment.setContent(dto.getContent());
        apComment.setArticleId(dto.getArticleId());
        apComment.setCreatedTime(new Date());
        apComment.setUpdatedTime(new Date());
        apComment.setLikes(0);
        apComment.setReply(0);
        apComment.setType((short) 0);
        apComment.setFlag((short) 0);
        mongoTemplate.save(apComment);
        return ResponseResult.okResult();
    }
}
