package com.heima.comment.service;

import com.heima.model.comment.dtos.CommentDTO;
import com.heima.model.comment.dtos.CommentLikeDTO;
import com.heima.model.comment.dtos.CommentSaveDTO;
import com.heima.model.common.dtos.ResponseResult;

/**
 * ClassName: CommentService
 * Package: com.heima.comment.service.impl
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/30 23:25
 * @Version 1.0
 */
public interface CommentService {
    /**
     * 保存评论
     * @return
     */
    public ResponseResult saveComment(CommentSaveDTO dto);

    /**
     * 点赞
     * @param dto
     * @return
     */
    public ResponseResult like(CommentLikeDTO dto);
    /**
     * 根据文章id查询评论列表
     * @param dto
     * @return
     */
    public ResponseResult loadComment(CommentDTO dto);
}
