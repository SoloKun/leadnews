package com.heima.feigns;

import com.heima.config.HeimaFeignAutoConfiguration;
import com.heima.feigns.fallback.BehaviorFeignFallback;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ClassName: BehaviorFeign
 * Package: com.heima.feigns
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 15:30
 * @Version 1.0
 */
@FeignClient(value = "leadnews-behavior",
        fallbackFactory = BehaviorFeignFallback.class,
        configuration = HeimaFeignAutoConfiguration.class)
public interface BehaviorFeign {
    @GetMapping("/api/v1/behavior_entry/one")
    public ResponseResult<ApBehaviorEntry> findByUserIdOrEquipmentId(@RequestParam("userId") Integer userId,
                                                                     @RequestParam("equipmentId") Integer equipmentId);
}
