package com.heima.model.common.dtos;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ClassName: PageRequestDTO
 * Package: com.heima.model.common.dtos
 * Description:
 * 用于封装分页请求参数，继承该类的子类可以直接使用分页参数。
 **/
@Data
@Slf4j
public class PageRequestDTO {
    @ApiModelProperty(value="当前页",required = true)
    protected Integer size;
    @ApiModelProperty(value="每页显示条数",required = true)
    protected Integer page;


    public void checkParam() {
        if (this.page == null || this.page <= 0) {
            setPage(1);
        }
        if (this.size == null || this.size <= 0 || this.size > 100) {
            setSize(10);
        }
    }
}
