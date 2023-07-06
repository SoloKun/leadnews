package com.heima.feigns.fallback;

import com.heima.feigns.BehaviorFeign;
import com.heima.model.behavior.pojos.ApBehaviorEntry;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: BehaviorFeignFallback
 * Package: com.heima.feigns.fallback
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/6 15:30
 * @Version 1.0
 */
@Slf4j
@Component
public class BehaviorFeignFallback implements FallbackFactory<BehaviorFeign> {


    @Override
    public BehaviorFeign create(Throwable e) {
        e.printStackTrace();
        return new BehaviorFeign() {
            @Override
            public ResponseResult<ApBehaviorEntry> findByUserIdOrEquipmentId(Integer userId, Integer equipmentId) {
                log.info("参数: {},{}",userId,equipmentId);
                log.error("BehaviorFeign findByUserIdOrEquipmentId 远程调用出错啦 ~~~ !!!! {} ",e.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR,"BehaviorFeign findByUserIdOrEquipmentId 远程调用出错啦 ~~~ !!!! {} ");
            }
        };
    }
}
