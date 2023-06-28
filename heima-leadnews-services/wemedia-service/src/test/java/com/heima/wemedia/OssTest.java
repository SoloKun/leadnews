package com.heima.wemedia;

import com.heima.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * ClassName: OssTest
 * Package: com.heima.wemedia
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/17 20:03
 * @Version 1.0
 */
@SpringBootTest
public class OssTest {
    @Autowired
    FileStorageService fileStorageService;
    @Value("${file.oss.prefix}")
    private String prefix;
    @Value("${file.oss.web-site}")
    private String webSite;
    @Test
    public void testUpload() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("C:\\Users\\92455\\Pictures\\imgs\\2.jpg");
        String store = fileStorageService.store(prefix, "2.jpg", fileInputStream);
        System.out.println(webSite+'/'+store);
    }
    @Test
    public void testDelete() {
        fileStorageService.delete("material/2023/6/20230617/2.jpg");
    }


}
