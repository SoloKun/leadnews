package com.heima.wemedia;

import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;

/**
 * @Description:
 * @Version: V1.0
 */
@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class OssTest {

    @Autowired
    FileStorageService fileStorageService;
    
    @Value("${file.oss.web-site}")
    String webSite;

    @Test
    public void testFileUpload() throws Exception {

        FileInputStream inputStream = new FileInputStream(new File("D:\\picture\\mv005.jpg"));
        String wemedia = fileStorageService.store("upload", "aaa1.jpg", inputStream);
        System.out.println(webSite+wemedia);
        // 删除文件
//        fileStorageService.delete("wemedia/2020/12/20201227/aaa1.jpg");
    }
}
