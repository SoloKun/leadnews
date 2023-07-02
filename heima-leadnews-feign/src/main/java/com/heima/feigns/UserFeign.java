package com.heima.feigns;

import com.heima.config.HeimaFeignAutoConfiguration;
import com.heima.feigns.fallback.UserFeignFallback;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ClassName: UserFeign
 * Package: com.heima.feigns
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/30 23:17
 * @Version 1.0
 */
@FeignClient(value = "leadnews-user",
        fallbackFactory = UserFeignFallback.class,
        configuration = HeimaFeignAutoConfiguration.class
)
public interface UserFeign {
    @GetMapping("/api/v1/user/{id}")
    ResponseResult<ApUser> findUserById(@PathVariable("id") Integer id);
}
