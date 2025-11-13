package com.yineng.bpe.redis.jedis;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Getting and setting Strings
 * SET stores a string value.
 * SETNX stores a string value only if the key doesn't already exist. Useful for implementing locks.
 * GET retrieves a string value.
 * MGET retrieves multiple string values in a single operation.
 *
 *
 * Managing counters
 * INCR atomically increments counters stored at a given key by 1.
 * INCRBY atomically increments (and decrements when passing a negative number) counters stored at a given key.
 * Another command exists for floating point counters: INCRBYFLOAT.
 *
 */
public class JedisStringTest {

    @Test
    void decrBy(){
        var jedis = JedisClient.jedis;
        //将 key 所储存的值加上给定的增量值（increment）
        //如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行
        var result = jedis.decrBy("decrby_num", 2);
        System.out.println("decrby_num:"+result);
    }

    @Test
    void decr(){
        //Decr 命令将 key 中储存的数字值减一。
        //如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作
        var jedis = JedisClient.jedis;
        var result = jedis.decr("decr_num");
        System.out.println("decr_num:"+result);
    }


    @Test
    void incrByFloat(){
        var jedis = JedisClient.jedis;
        //INCRBYFLOAT以原子方式递增（并在传递负数时递减）存储在给定键处的计数器
        //这里是浮点，有精度损失
        //第二个参数是增量
        //返回递增后的值
        var z =  jedis.incrByFloat("incrbyfloat_num", 0.1);
        System.out.println("incrbyfloat_num:"+z);
    }

    @Test
    void incrBy(){
        var jedis = JedisClient.jedis;
        //INCRBY以原子方式递增（所储存的值加上给定的增量值）
        //第二个参数是增量
        //没有指定具体值从0开始
        var z =  jedis.incrBy("incrby_num", 1);
        System.out.println("incrby_num:"+z);
    }

    @Test
    void incr(){
        var jedis = JedisClient.jedis;
        //INCR以原子方式将给定键中存储的计数器加 1
        //返回递增后的值
        var result =  jedis.incr("incr_num");
        System.out.println("incr_num:"+result);//
    }

    @Test
    void setx(){
        var jedis = JedisClient.jedis;
        //仅当键尚不存在时， SETNX才会存储字符串值
        var r = jedis.setnx("nx_num6", "10");
        System.out.println("nx_num5:"+r);//成功设置返回1，否则返回0
    }

    @Test
    void setex(){
        var jedis = JedisClient.jedis;
        //仅当键尚不存在时， SETNX才会存储字符串值
        var r = jedis.setex("ex_num", 10,"expireable");
        System.out.println("ex_num:"+r);//ex_num:OK
    }

    @Test
    void getSet(){
        var jedis = JedisClient.jedis;
        // GETSET命令将键设置为新值，并返回旧值作为结果
        var old =  jedis.getSet("bike:4", "Deimos");
        System.out.println(old);
    }

    @Test
    void get(){
        var jedis = JedisClient.jedis;
        var user =  jedis.get("bike:4");
        System.out.println(user);
    }

    @Test
    void mget(){
        var jedis = JedisClient.jedis;
        String res5 = jedis.mset("bike:1", "Deimos", "bike:2", "Ares", "bike:3", "Vanth");
        System.out.println(res5); // OK
        List<String> res6 = jedis.mget("bike:1", "bike:2", "bike:3");
        System.out.println(res6); // [Deimos, Ares, Vanth]

    }
}
