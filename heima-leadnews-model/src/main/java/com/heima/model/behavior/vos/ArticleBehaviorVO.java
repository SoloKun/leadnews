package com.heima.model.behavior.vos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel("文章行为VO")
@AllArgsConstructor
@NoArgsConstructor
public class ArticleBehaviorVO {
    @ApiModelProperty("是否点赞")
    private Boolean islike;
    @ApiModelProperty("是否关注")
    private Boolean isfollow;
    @ApiModelProperty("是否不喜欢")
    private Boolean isunlike;
    @ApiModelProperty("是否收藏")
    private Boolean iscollection;
}