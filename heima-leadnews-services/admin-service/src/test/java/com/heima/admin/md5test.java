package com.heima;

import org.apache.commons.codec.cli.Digest;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

/**
 * ClassName: md5test
 * Package: com.heima
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 21:09
 * @Version 1.0
 */
public class md5test {
    @Test
    public void test(){
        String str = DigestUtils.md5DigestAsHex("123456".getBytes());
        System.out.println(str);

    }
}
