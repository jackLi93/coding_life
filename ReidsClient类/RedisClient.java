package com.jack.codelife;
// import some class...
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

//一个简易版的RedisClient
public class RedisClient
{
	private final static Logger log = LoggerFactory.getLogger("RedisClient"); 
	private JedisPool pool;
	private boolean broken = false;

	//初始化
	public boolean init(String redisIp,int redisPort,int timeWait,int timeout,int maxActive int maxIdle,boolean testOnBorrow){                      
        // 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大等待时间，单位毫秒
        config.setMaxWaitMillis(timeWait);
        // 设置最大连接数
        config.setMaxTotal(maxActive);
        // 设置最大空余连接数
        config.setMaxIdle(maxIdle);
        // 在borrow一个jedis实例时，是否提前进行alidate操作；如果为true，则得到的jedis实例均是可用的
        config.setTestOnBorrow(testOnBorrow);
        // 创建连接池
        //pool = new JedisPool(config, redisIp, redisPort);
        pool = new JedisPool(config, redisIp, redisPort, timeout);
        return true;
	}
	//结束
	public void fini(){
		pool.destroy();
	}
	
	//getJedis
	public Jedis getJedis(){
        Jedis jedisResource = null;
        try {
            if (pool != null) {
                jedisResource = pool.getResource();
                return jedisResource;
            } else {
                return null;
            }
        } catch (Exception e) {
            if (jedisResource != null) {
                pool.returnBrokenResource(jedisResource);
            }
            log.error("jedis getResource exception " + e);
            e.printStackTrace();
            return null;
        }
	}

	//relase jedis
	public void release(Jedis jedis){
	 if (null != jedis) {
            try {
                pool.returnResource(jedis);
            } catch (Exception e) {
                if (null != jedis) {
                    pool.returnBrokenResource(jedis);
                }
                e.printStackTrace();
                log.error("jedis returnResource exception" + e);
            }
        }
	}
	
	//redis brokenjedis
	public void releaseBroken(Jedis jedis){
	 if (null != jedis) {
            try {
                pool.returnBrokenResource(jedis);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("jedis returnBrokenResource exception" + e);
            }
        }
	}

	//测试入口
	public void main(String []args){
		RedisClient client = new RedisClient();
		clinet.init("127.0.0.1",8080,10000, 100, 20, 10, false);//初始化
		Jedis jedis = client.getJedis();
		...
		//jedis的若干操作
		...
		client.release(jedis);
		client.fini();
	}

}