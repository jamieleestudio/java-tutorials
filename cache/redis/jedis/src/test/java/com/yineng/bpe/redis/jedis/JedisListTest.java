package com.yineng.bpe.redis.jedis;

import org.junit.jupiter.api.Test;

/**
 *
 * Redis lists are linked lists of string values. Redis lists are frequently used to:
 *
 * Implement stacks and queues.
 * Build queue management for background worker systems.
 *
 *
 * Basic commands
 * LPUSH adds a new element to the head of a list; RPUSH adds to the tail.
 * LPOP removes and returns an element from the head of a list; RPOP does the same but from the tails of a list.
 * LLEN returns the length of a list.
 * LMOVE atomically moves elements from one list to another.
 * LRANGE extracts a range of elements from a list.
 * LTRIM reduces a list to the specified range of elements.
 *
 */
public class JedisListTest {


    @Test
    void lpush(){
        var jedis = JedisClient.jedis;
        //在头部添加
        var result  = jedis.lpush("queue","one");
        //返回列表的长度
        System.out.println(result);
        System.out.println(jedis.lrange("queue",0,-1));
    }

    @Test
    void rpush(){
        var jedis = JedisClient.jedis;
        //在尾部添加
        var result  = jedis.rpush("queue","two");
        //返回列表的长度
        System.out.println(result);
        System.out.println(jedis.lrange("queue",0,-1));
    }

    @Test
    void lpop(){
        var jedis = JedisClient.jedis;
        //从列表头部删除并返回一个元素
        var result  = jedis.lpop("queue");
        System.out.println(result);
        System.out.println(jedis.lrange("queue",0,-1));
    }

    @Test
    void rpop(){
        var jedis = JedisClient.jedis;
        //从列表尾部删除并返回一个元素
        var result  = jedis.rpop("queue");
        System.out.println(result);
        System.out.println(jedis.lrange("queue",0,-1));
    }

}
