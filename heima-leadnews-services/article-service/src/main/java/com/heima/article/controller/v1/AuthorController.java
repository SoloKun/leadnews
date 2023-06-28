package com.heima.article.controller.v1;



import com.heima.article.service.impl.AuthorServiceImpl;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: AuthorController
 * Package: com.heima.article.controller.v1
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 16:50
 * @Version 1.0
 */
@Api(value = "作者管理", tags = "作者管理")
@RestController
@RequestMapping("/api/v1/author")
public class AuthorController {
    @Autowired
    private AuthorServiceImpl authorService;

    @ApiOperation("根据名称查询作者")
    @ApiParam(name = "name", value = "作者名称", required = true)
    @GetMapping("/findByUserId/{userId}")
    public ResponseResult findByUserId(@PathVariable("userId") Integer userId) {
        return authorService.findByUserId(userId);
    }

    @ApiOperation("保存作者")
    @ApiParam(name = "apAuthor", value = "作者对象", required = true)
    @PostMapping("/save")
    public ResponseResult save(@RequestBody ApAuthor apAuthor) {
        return authorService.saveUser(apAuthor);
    }
}