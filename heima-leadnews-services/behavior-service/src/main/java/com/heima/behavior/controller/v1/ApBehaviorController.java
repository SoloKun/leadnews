package com.heima.behavior.controller.v1;

import com.heima.behavior.service.*;
import com.heima.model.behavior.dtos.*;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.validator.ValidatorUpdateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ApBehaviorController
 * Package: com.heima.behavior.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/29 18:39
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@Api(value = "行为模块管理", tags = "行为模块Controller")
public class ApBehaviorController {
    @Autowired
    ApLikesBehaviorService apLikesBehaviorService;
    @Autowired
    ApReadBehaviorService apReadBehaviorService;
    @Autowired
    ApUnlikeBehaviorService apUnlikesBehaviorService;
    @Autowired
    ApCollectionBehaviorService apCollectionBehaviorService;
    @Autowired
    ApArticleBehaviorService apArticleBehaviorService;
    @ApiOperation("保存或取消点赞行为")
    @PostMapping("/likes_behavior")
    public ResponseResult likesBehavior(@RequestBody @Validated(ValidatorUpdateGroup.class) LikesBehaviorDTO likesBehaviorDTO) {
        return apLikesBehaviorService.like(likesBehaviorDTO);
    }
    @ApiOperation("保存阅读行为")
    @PostMapping("/read_behavior")
    public ResponseResult readBehavior(@RequestBody @Validated ReadBehaviorDTO dto) {
        return apReadBehaviorService.readBehavior(dto);
    }
    @ApiOperation("不喜欢行为")
    @PostMapping("/un_likes_behavior")
    public ResponseResult unlikesBehavior(@RequestBody @Validated UnLikesBehaviorDTO dto) {
        return apUnlikesBehaviorService.unlikeBehavior(dto);
    }

    @ApiOperation("收藏行为")
    @PostMapping("/collection_behavior")
    public ResponseResult collectionBehavior(@RequestBody @Validated CollectionBehaviorDTO dto) {
        return apCollectionBehaviorService.collectBehavior(dto);
    }

    @ApiOperation("文章行为")
    @PostMapping("/article/load_article_behavior")
    public ResponseResult loadArticleBehavior(@RequestBody @Validated ArticleBehaviorDTO articleBehaviorDTO) {
        return apArticleBehaviorService.loadArticleBehavior(articleBehaviorDTO);
    }

}
