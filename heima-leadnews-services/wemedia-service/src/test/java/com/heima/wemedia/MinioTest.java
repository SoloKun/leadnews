package com.heima.wemedia;

import com.heima.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * ClassName: MinioTest
 * Package: com.heima.wemedia
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/28 14:28
 * @Version 1.0
 */
@SpringBootTest
public class MinioTest {
    @Resource(name = "minIOFileStorageService")

    private FileStorageService fileStorageService;
    @Value("${file.minio.readPath}")
    String readPath;
    @Test
    public void testMinio() throws FileNotFoundException {
        fileStorageService.store("test","1.png","image/png",new FileInputStream("C:\\Users\\92455\\Pictures\\Saved Pictures\\s\\5E7j7G3MUufwz7vMnCTrbAtx.jpeg"));

    }
}
