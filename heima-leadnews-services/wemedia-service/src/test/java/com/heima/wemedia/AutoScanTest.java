package com.heima.wemedia;

import com.heima.wemedia.service.WmNewsAutoScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ClassName: AutoScanTest
 * Package: com.heima.wemedia
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/20 21:06
 * @Version 1.0
 */
@SpringBootTest
public class AutoScanTest {
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Test
    public void testAutoScan(){
        wmNewsAutoScanService.autoScanByMediaNews(6286);
    }
}
