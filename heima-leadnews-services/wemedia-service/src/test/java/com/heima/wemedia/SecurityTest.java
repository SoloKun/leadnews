package com.heima.wemedia;

import com.heima.aliyun.scan.GreenTextScan;
import com.heima.aliyun.scan.GreenTextScan;
import org.apache.catalina.security.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * ClassName: SecurityTest
 * Package: com.heima.wemedia
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 14:32
 * @Version 1.0
 */
@SpringBootTest
public class SecurityTest {
    @Autowired
    private GreenTextScan greenTextScan;
    @Test
    public void testScan() throws Exception {
        String text = "8964 test";
        Map map = greenTextScan.greenTextScan(text);
        System.out.println(map);
    }

}
