package com.heima.feigns;

import com.heima.config.HeimaFeignAutoConfiguration;
import com.heima.feigns.fallback.WemediaFeignFallback;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: WemediaFeign
 * Package: com.heima.feigns
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/15 15:39
 * @Version 1.0
 */
@FeignClient(
        value = "leadnews-wemedia",
        fallbackFactory = WemediaFeignFallback.class,
        configuration = HeimaFeignAutoConfiguration.class
)
public interface WemediaFeign {
    @GetMapping("/api/v1/user/findByName/{name}")
    public ResponseResult<WmUser> findByName(@PathVariable("name") String name);
    @PostMapping("/api/v1/user/save")
    public ResponseResult<WmUser>save(@RequestBody WmUser wmUser);

    @PutMapping("/api/v1/news/update")
    public ResponseResult updateWmNews(@RequestBody WmNews wmNews);

    @GetMapping("/api/v1/news/one/{id}")
    public ResponseResult<WmNews> findWmNewsById(@PathVariable("id") Integer id);
}
