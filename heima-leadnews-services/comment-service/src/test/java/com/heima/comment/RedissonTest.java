package com.heima.comment;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: RedissonTest
 * Package: com.heima
 * Description:
 *
 * @Author solokun
 * @Create 2023/7/3 12:23
 * @Version 1.0
 */
@SpringBootTest
public class RedissonTest {
    public RedissonClient redissonClient() {
        Config config = new Config();
        //指定使用单节点部署方式
        config.useSingleServer()
                .setAddress("redis://192.168.199.115:6379");
        config.useSingleServer().setConnectionPoolSize(500);//设置对于master节点的连接池中连接数最大为500
        config.useSingleServer().setIdleConnectionTimeout(10000);//如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
        config.useSingleServer().setConnectTimeout(30000);//同任何节点建立连接时的等待超时。时间单位是毫秒。
        config.useSingleServer().setTimeout(3000);//等待节点回复命令的时间。该时间从命令发送成功时开始计时。
        config.useSingleServer().setPingTimeout(30000);
        config.useSingleServer().setPassword("root");
        //获取RedissonClient对象
        RedissonClient redisson = Redisson.create(config);
        //获取锁对象
        return redisson;
    }


    @Test
    public void test() throws InterruptedException {
        // lockTest();
        // lockTest2();
        // lockTest3();
        // lockTest4();
        lockTest5();
    }

    public void lockTest(){
        RedissonClient redissonClient = redissonClient();
        RLock mylock = redissonClient.getLock("mylock");
        mylock.lock();
        try {
            System.out.println("执行加锁的同步代码块");
        } finally {
            mylock.unlock();
        }
    }

    public void lockTest2(){
        new Thread(()->{
            RedissonClient redissonClient = redissonClient();
            RLock mylock = redissonClient.getLock("mylock");
            System.out.println("线程1 尝试获取锁");
            mylock.lock();
            try {
                System.out.println("线程1 执行加锁的同步代码块");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mylock.unlock();
            }
        }).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            RedissonClient redissonClient = redissonClient();
            RLock mylock = redissonClient.getLock("mylock");
            System.out.println("线程2 尝试获取锁");
            mylock.lock();
            try {
                System.out.println("线程2 执行加锁的同步代码块");
            } finally {
                mylock.unlock();
            }
        }).start();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void lockTest3(){
        RedissonClient redissonClient = redissonClient();
        RLock mylock = redissonClient.getLock("mylock");
        System.out.println("准备获取锁");
        mylock.lock();
        System.out.println("第一次加锁成功 ");
        mylock.lock();
        System.out.println("第二次加锁成功 ");
        mylock.lock();
        System.out.println("第三次加锁成功 ");
        mylock.unlock();
        System.out.println("第一次解锁成功 ");
        mylock.unlock();
        System.out.println("第二次解锁成功 ");
        mylock.unlock();
        System.out.println("第三次解锁成功 ");
    }

    public void lockTest4(){
        RedissonClient redissonClient = redissonClient();
        RLock mylock = redissonClient.getLock("mylock");

        mylock.lock();
        System.out.println("加锁成功  要执行100秒");
        try {
            TimeUnit.SECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mylock.unlock();
        System.out.println("解锁成功");
    }
    public void lockTest5() throws InterruptedException{
        new Thread(()->{
            RedissonClient redissonClient = redissonClient();
            RLock mylock = redissonClient.getLock("mylock");
            System.out.println("线程1 尝试获取锁");
            mylock.lock();
            try {
                System.out.println("线程1 执行加锁的同步代码块");
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mylock.unlock();
            }
        }).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(()->{
            RedissonClient redissonClient = redissonClient();
            RLock mylock = redissonClient.getLock("mylock");
            System.out.println("线程2 尝试获取锁");
            boolean b = mylock.tryLock();
            if (b) {
                System.out.println("线程2 执行加锁的同步代码块");
                mylock.unlock();
            }else {
                System.out.println("抢锁失败 老子不等了");
            }
        }).start();
        Thread.sleep(20000);
    }
}

