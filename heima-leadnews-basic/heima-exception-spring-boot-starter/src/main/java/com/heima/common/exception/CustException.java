package com.heima.common.exception;

import com.heima.model.common.enums.AppHttpCodeEnum;

/**
 * ClassName: CustException
 * Package: com.heima.common.exception
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 15:05
 * @Version 1.0
 */
public class CustException {
    public static void cust(AppHttpCodeEnum codeEnum) {
        throw new CustomException(codeEnum );
    }
    public static void cust(AppHttpCodeEnum codeEnum,String msg) {
        throw new CustomException(codeEnum,msg);
    }
}
