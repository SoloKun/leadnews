package com.heima.user;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: UserApplication
 * Package: com.heima.user
 * Description:
 *
 * @Author solokun
 * @Create 2023/6/14 22:55
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.heima.user.mapper")
public class UserApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(UserApplication.class,args);
    }

    @Bean
    // 分页插件
    public PaginationInterceptor paginationInterceptor(){
        //PaginationInterceptor是MybatisPlus提供的分页插件，用于实现分页功能，它继承了Mybatis的Interceptor接口，所以它也是一个拦截器。

        return new PaginationInterceptor();
    }

}

