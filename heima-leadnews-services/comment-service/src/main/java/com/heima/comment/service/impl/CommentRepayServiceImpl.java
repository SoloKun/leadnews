package com.heima.comment.service.impl;

import com.heima.aliyun.scan.GreenTextScan;
import com.heima.comment.service.CommentRepayService;
import com.heima.common.exception.CustException;
import com.heima.feigns.AdminFeign;
import com.heima.feigns.ArticleFeign;
import com.heima.feigns.UserFeign;
import com.heima.model.comment.dtos.CommentRepayDTO;
import com.heima.model.comment.dtos.CommentRepayLikeDTO;
import com.heima.model.comment.dtos.CommentRepaySaveDTO;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentRepay;
import com.heima.model.comment.pojos.ApCommentRepayLike;
import com.heima.model.comment.vos.ApCommentRepayVO;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.threadlocal.AppThreadLocalUtils;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.SensitiveWordUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: CommentRepayServiceImpl
 * Package: com.heima.comment.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/1 19:49
 * @Version 1.0
 */
@Service
public class CommentRepayServiceImpl implements CommentRepayService {
    @Autowired
    private AdminFeign adminFeign;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    MongoTemplate mongoTemplate;
    @Override
    public ResponseResult loadCommentRepay(CommentRepayDTO dto) {
            List<ApCommentRepay> repays = new ArrayList<>();
            if(dto.getSize()==null||dto.getSize()==0){
                dto.setSize(10);
            }
            String commentId = dto.getCommentId();
            Date minDate = dto.getMinDate();
        Query query = Query.query(Criteria.where("commentId").is(commentId)
                        .and("createdTime").lt(minDate))
                .limit(dto.getSize())
                .with(Sort.by(Sort.Direction.DESC, "likes"))
                .with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApCommentRepay> apCommentRepays = mongoTemplate.find(query, ApCommentRepay.class);
        ApUser user = AppThreadLocalUtils.getUser();

        if(user==null||user.getId()==0){
            //未登录
            return ResponseResult.okResult(apCommentRepays);
        }
        List<String> userIds = apCommentRepays.stream()
                .map(ApCommentRepay::getId).collect(Collectors.toList());
        Integer userId = user.getId();
        List<ApCommentRepayLike> apCommentRepayLikes = mongoTemplate.find(Query.query(
                Criteria.where("authorId").is(userId)
                .and("commentRepayId").in(userIds)), ApCommentRepayLike.class);

        List<ApCommentRepayVO> apCommentRepayVOS = apCommentRepays.stream()
                .map(apCommentRepay -> {
                    ApCommentRepayVO apCommentRepayVO = new ApCommentRepayVO();
                    BeanUtils.copyProperties(apCommentRepay, apCommentRepayVO);
                    for(ApCommentRepayLike cur:apCommentRepayLikes){
                            if(cur.getCommentRepayId().equals(apCommentRepay.getId())){
                                apCommentRepayVO.setOperation((short)0);
                                break;
                            }
                    }
                    return apCommentRepayVO;
                }).collect(Collectors.toList());
        return ResponseResult.okResult(apCommentRepayVOS);
    }

    @Override
    public ResponseResult saveCommentRepay(CommentRepaySaveDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        Integer userId = user.getId();
        if(userId.intValue()==0){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        String content = dto.getContent();
        ResponseResult<List<String>> result = adminFeign.selectAllSensitives();
        if(!result.checkCode()){
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, "远程调用admin服务器错误");
        }
        List<String> sensitiveList = result.getData();
        SensitiveWordUtil.initMap(sensitiveList);
        Map<String, Integer> sensitiveWordMap = SensitiveWordUtil.matchWords(content);
        if(sensitiveWordMap!=null&&sensitiveWordMap.size()>0){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "评论内容包含敏感词:"+sensitiveWordMap.keySet());
        }
        try{
            Map map = greenTextScan.greenTextScan(content);
            String suggestion = (String) map.get("suggestion");
            if("block".equals(suggestion)){
                content = (String) map.get("filteredContent");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, "远程调用阿里云服务器错误");
        }
        if(StringUtils.isBlank(content)){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "评论内容不能为空");
        }
        ResponseResult<ApUser> userResult = userFeign.findUserById(userId);
        if(!userResult.checkCode()){
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, "远程调用user服务器错误");
        }
        ApUser apUser = userResult.getData();
        Query query = Query.query(Criteria.where("id").is(dto.getCommentId()));

        ApComment apComment = mongoTemplate.findOne(query, ApComment.class);
        if(apComment==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "评论不存在");
        }
        ApCommentRepay apCommentRepay = new ApCommentRepay();
        apCommentRepay.setAuthorId(userId);
        apCommentRepay.setAuthorName(apUser.getName());
        apCommentRepay.setCommentId(dto.getCommentId());
        apCommentRepay.setContent(content);
        apCommentRepay.setLikes(0);
        apCommentRepay.setLongitude(null);
        apCommentRepay.setLatitude(null);
        apCommentRepay.setAddress(null);
        apCommentRepay.setCreatedTime(new Date());
        apCommentRepay.setUpdatedTime(new Date());
        mongoTemplate.insert(apCommentRepay);
        apComment.setReply(apComment.getReply()+1);
        mongoTemplate.save(apComment);
        return ResponseResult.okResult();
    }

    @Override
    public ResponseResult saveCommentRepayLike(CommentRepayLikeDTO dto) {
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        Integer userId = user.getId();
        if(userId.intValue()==0){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "需要登录");
        }
        Short operation = dto.getOperation();
        Query query = Query.query(Criteria.where("id").is(dto.getCommentRepayId()));
        ApCommentRepay apCommentRepay = mongoTemplate.findOne(query, ApCommentRepay.class);
        if(apCommentRepay==null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "回复不存在");
        }
        ApCommentRepayLike apCommentRepayLike = mongoTemplate.findOne(Query.query(Criteria.where("authorId").is(userId)
                .and("commentRepayId").is(dto.getCommentRepayId())), ApCommentRepayLike.class);
        if(operation==0) {
            if(apCommentRepayLike!=null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "不能重复点赞");
            }
            apCommentRepayLike = new ApCommentRepayLike();
            apCommentRepayLike.setCommentRepayId(dto.getCommentRepayId());
            apCommentRepayLike.setOperation(operation);
            apCommentRepayLike.setAuthorId(userId);
            mongoTemplate.insert(apCommentRepayLike);
            apCommentRepay.setLikes(apCommentRepay.getLikes()+1);
        }else{
            if(apCommentRepayLike==null){
                CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "不能重复取消点赞");
            }
            mongoTemplate.remove(apCommentRepayLike);
            Integer likes = apCommentRepay.getLikes();
            if(likes>0){
                apCommentRepay.setLikes(likes-1);
            }else{
                apCommentRepay.setLikes(0);
            }

        }
        mongoTemplate.save(apCommentRepay);
        Map<String, Object> map = new HashMap<>();
        map.put("likes",apCommentRepay.getLikes());
        return ResponseResult.okResult(map);
    }
}
