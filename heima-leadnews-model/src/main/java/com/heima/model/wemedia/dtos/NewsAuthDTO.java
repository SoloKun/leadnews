package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDTO;
import lombok.Data;

/**
 * ClassName: NewsAuthDto
 * Package: com.heima.model.wemedia.dtos
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 23:04
 * @Version 1.0
 */
@Data
public class NewsAuthDTO extends PageRequestDTO {
    /**
     * 文章标题
     */
    private String title;
    /**
     * 状态
     */
    private Short status;
    /**
     * 文章id
     */
    private Integer id;
    /**
     * 失败原因
     */
    private String msg;
}
