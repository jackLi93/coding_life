package com.jack.codelife;
//import some classes...
import redis.clients.jedis.Jedis;//jedis
import redis.clients.jedis.JedisPool;//redis pool
import redis.clients.jedis.JedisPoolConfig;//配置

public final  class RedisUtil
{
    public static String  redisIp = "127.0.0.1"; //redis服务器的ip
    public static int redisPort = 6688;//redis服务器的端口号
    public static String auth = "admin"; //访问密码
    public static int maxActive =1000;//可用连接最大数目，默认值为8，如果赋值为-1，则表示不受限制。
    public static int maxIdle = 200;// //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    public static int timeOut = 20000;
    public static int timeWait =30000;//等待可用连接的最大时间，单位毫秒,默认值为-1，表示永不超时
    public static boolean borrow = false;//在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    public static JedisPool jedisPool = null;
    /**
     **初识Redis连接池
     **/
    static {
        try{
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxWaitMillis(timeWait);
            config.setMaxTotal(maxActive);
           // config.setMaxActive(maxActive);
            config.setMaxIdle(maxIdle);
           // config.setMaxWait(timeWait);
            config.setTestOnBorrow(borrow);
           // jedisPool = new JedisPool(config,redisIp,redisPort,timeOut,auth);
            jedisPool = new  JedisPool(config, redisIp, redisPort, timeOut);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     **获取Jedis实例
     **/
    public synchronized static Jedis getJedis(){

        try{
            if(jedisPool!=null){
                Jedis jedis = jedisPool.getResource();
                return jedis ;
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }
    /**
     **释放Jedis资源
     **/
    public static void returnResource(Jedis jedis){
        if(jedis!=null){
            jedisPool.returnResource(jedis);
        }
    }

    public static void main(String []args){

        RedisUtil.getJedis().set("name","jacklee");
        System.out.println(RedisUtil.getJedis().get("name"));//-->jacklee;
    }
}


===================运行程序======================

1.第一次运行程序报错，修改：
           // jedisPool = new JedisPool(config,redisIp,redisPort,timeOut,auth);
            jedisPool = new  JedisPool(config, redisIp, redisPort, timeOut);
		注释带auth的参数后即可。
2.程序输出为: jacklee

3.使用JedisPool等类的时候需要添加依赖：maven依赖如下；

		<dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
			<version>2.7.3</version>
		</dependency>