package com.heima.wemedia;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MinIoTest {
    // 指定MinIo实现
    @Resource(name = "minIOFileStorageService")
    FileStorageService fileStorageService;
    // 不指定 beanName 注入的是OSS的实现
    @Autowired
    FileStorageService fileStorageService2;
    @Test
    public void uploadToMinIo() throws FileNotFoundException {
        System.out.println(fileStorageService);
        System.out.println(fileStorageService2);
        // 准备好一个静态页
        FileInputStream fileInputStream = new FileInputStream("D://list.html");
        // 将静态页上传到minIO文件服务器中          文件名称            文件类型             文件流
        fileStorageService.store("article","list.html","text/html",fileInputStream);
    }
}
