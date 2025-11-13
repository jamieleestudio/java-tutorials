package test;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

public class RedisUtils {

    private static Jedis jedis = new Jedis(new HostAndPort("localhost",6379));

    public static Jedis getJedis(){
        return jedis;
    }

}
