package com.heima.feigns.fallback;

import com.heima.feigns.UserFeign;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: UserFeignFallback
 * Package: com.heima.feigns.fallback
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/30 23:18
 * @Version 1.0
 */
@Component
@Slf4j
public class UserFeignFallback implements FallbackFactory<UserFeign> {

    @Override
    public UserFeign create(Throwable throwable) {
        throwable.printStackTrace();
        return new UserFeign() {
            @Override
            public ResponseResult<ApUser> findUserById(Integer id) {
                log.error("参数: {}",id);
                log.error("UserFeign findUserById 远程调用出错啦 ~~~ !!!! {} ",throwable.getMessage());
                return ResponseResult.errorResult(AppHttpCodeEnum.REMOTE_SERVER_ERROR,throwable.getMessage());
            }
        };
    }
}
