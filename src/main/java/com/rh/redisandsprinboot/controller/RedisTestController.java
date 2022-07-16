package com.rh.redisandsprinboot.controller;

import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 整合redis
 */
@RestController
public class RedisTestController {

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("redisTest")
    public String testRedis(){
        redisTemplate.opsForValue().set("name","lucy");
        System.out.println("测试master");
        System.out.println("测试hot-fix1");
        System.out.println("测试1");
        System.out.println("测试hot-fix");
        return (String) redisTemplate.opsForValue().get("name");
    }


    @GetMapping("testLock")
    public void testLock(){
        String uuid = UUID.randomUUID().toString();
        //1、获取锁，setne
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,3, TimeUnit.SECONDS);
        //获取锁成功、查询num的值
        if (lock){
            Object value = redisTemplate.opsForValue().get("num");
            //2.1判断num为空就return
            if (StringUtils.isEmpty(value)){
                return;
            }

            //2.2有值就转换为int
            int num = Integer.parseInt(value + "");
            //2.3把redis的num加1
            redisTemplate.opsForValue().set("num",++num);
            //2.4释放锁，del
            //判断比较uuid值是否一样
            String lockUuid = (String) redisTemplate.opsForValue().get("lock");
            if (lockUuid.equals(uuid)) {
                redisTemplate.delete("lock");
            }

        }else {
            //3.获取锁失败，每隔0.1秒再获取

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testLock();
        }
        
    }
}
