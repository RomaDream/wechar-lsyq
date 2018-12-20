package com.nk.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import com.nk.common.Configuration;

/**
 * redis数据库连接工具
 * 
 * @author jj
 *
 */
public class RedisDB {
	private static Logger log = LoggerFactory.getLogger(RedisDB.class);
	/*最大连接数*/
	private static int maxTotal = Integer.parseInt(Configuration.propMap.get("redis.maxTotal"));
	/*最大空闲等待数*/
	private static int maxIdle = Integer.parseInt(Configuration.propMap.get("redis.maxIdle"));
	/*最大等待时间*/
	private static int maxWaitMillis = Integer.parseInt(Configuration.propMap.get("redis.maxWaitMillis"));
	/*从pool中获取连接时，是否检查连接可用*/
	private static boolean testOnBorrow = Boolean.parseBoolean(Configuration.propMap.get("redis.testOnBorrow"));
	/*端口号*/
	private static int port = Integer.parseInt(Configuration.propMap.get("redis.port"));
	/*ip地址*/
	private static String hostName = Configuration.propMap.get("redis.hostName");
	/*redis连接密码*/
	private static String password = Configuration.propMap.get("redis.password");
	/*是否对空闲连接对象进行检查*/
	private static boolean testOnIdle = Configuration.propMap.get("redis.testOnIdle").equalsIgnoreCase("true")?true:false;
	/*每隔多少秒检查一次空闲连接对象*/
	private static int timeBetweenEvictionRunsMillis = Integer.valueOf(Configuration.propMap.get("redis.timeBetweenEvictionRunsMillis"));
	/*一次驱逐过程中最多驱逐对象的个数*/
	private static int numTestsPerEvictionRun = Integer.parseInt(Configuration.propMap.get("redis.numTestsPerEvictionRun"));
	/*表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义*/
	private static int minEvictableIdleTimeMillis = Integer.parseInt(Configuration.propMap.get("redis.minEvictableIdleTimeMillis"));
	/*连接超时*/
	private static int timeout = Integer.parseInt(Configuration.propMap.get("redis.timeout"));
	
	/*jedis连接池对象*/
	private static JedisPool jedisPool;
	private static Object lock = new Object();
	
	static{
		inti();
	}
	
	/**
	 * 初始化连接池
	 * */
	private static void inti(){
		if(null==jedisPool){
			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			config.setMaxTotal(maxTotal);
			config.setMaxIdle(maxIdle);
			config.setMaxWaitMillis(maxWaitMillis);
			
			config.setTestOnBorrow(testOnBorrow);
			config.setTestWhileIdle(testOnIdle);
			config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			config.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
			synchronized (lock) {
				try {
					jedisPool = new JedisPool(config, hostName, port, timeout, password);
					log.debug("inti jedis pool success!");
				} catch (Exception e) {
					log.debug("inti jedis pool fail");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 获取jedis对象
	 */
	public static Jedis getJedisConn(){
		if(null==jedisPool){
			inti();
		}
		return jedisPool.getResource();
	}
	
	/**
	 * 返回给连接池
	 */
	public static void closeJedis(Jedis jedis){
		if(null!=jedis){
			jedis.close();
		}
	}

	public static boolean isExists(String key,String field){
		Jedis jedis = null;
		boolean b = false;
		try {
			jedis = RedisDB.getJedisConn();
			jedis.select(3);
			return jedis.hexists(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisDB.closeJedis(jedis);
		}
		return b;
	}

	public static String getObject(String key,String field){
		String values = null;
		Jedis jedis = null;
		try {
			jedis = RedisDB.getJedisConn();
			jedis.select(3);
			values = jedis.hget(key, field);
//	    	System.out.println("从redis中获取数据。。。");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			RedisDB.closeJedis(jedis);
		}
		return values;
	}
}
