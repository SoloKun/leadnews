package com.heima.common.exception;

import com.heima.model.common.enums.AppHttpCodeEnum;

/**
 * ClassName: CustomException
 * Package: com.heima.common.exception
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 14:50
 * @Version 1.0
 */
public class CustomException extends RuntimeException{
    // 异常处理的枚举
    private AppHttpCodeEnum appHttpCodeEnum;

    public CustomException(AppHttpCodeEnum appHttpCodeEnum) {
        this.appHttpCodeEnum = appHttpCodeEnum;
    }
    public CustomException(AppHttpCodeEnum appHttpCodeEnum,String msg) {
        appHttpCodeEnum.setErrorMessage(msg);
        this.appHttpCodeEnum = appHttpCodeEnum;
    }
    public AppHttpCodeEnum getAppHttpCodeEnum() {
        return appHttpCodeEnum;
    }
}
