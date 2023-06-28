package com.heima.feigns.fallback;

import com.heima.feigns.AdminFeign;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ClassName: AdminFeignFallback
 * Package: com.heima.feigns.fallback
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 16:32
 * @Version 1.0
 */
@Component
@Slf4j
public class AdminFeignFallback implements FallbackFactory<AdminFeign> {
    @Override
    public AdminFeign create(Throwable throwable) {
        throwable.printStackTrace();
        return new AdminFeign() {
            @Override
            public ResponseResult<List<String>> selectAllSensitives() {
                log.error("AdminFeign selectAllSensitives 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }

            @Override
            public ResponseResult<AdChannel> findOne(Integer id) {
                log.error("AdminFeign findOne 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR);
            }
        };
    }
}
