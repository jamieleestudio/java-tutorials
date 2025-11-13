package com.yineng.bpe.redis.jedis;

import redis.clients.jedis.JedisPooled;

/**
 * Hello world!
 *
 */
public class JedisClient {

    public static JedisPooled jedis = new JedisPooled("localhost", 6379);

}
