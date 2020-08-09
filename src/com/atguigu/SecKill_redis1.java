package com.atguigu;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

//实验一，存在超卖情况
public class SecKill_redis1 {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SecKill_redis1.class);

	public static void main(String[] args) {

		Jedis jedis = new Jedis("192.168.137.128", 6379);

		System.out.println(jedis.ping());

		jedis.close();

	}

	//秒杀业务代码
	public static boolean doSecKill(String uid, String prodid) throws IOException {

		//1.准备往redis中存储数据的key名称
		String qtKey = "sk:" + prodid + ":qt"; // 库存
		String userKey = "sk:" + prodid + ":user"; // 秒杀成功用户

		//2.判断用户是否已经秒杀过，如果秒杀成功了，就不能在进行秒杀了。
		Jedis jedis = new Jedis("192.168.137.128", 6379);
		if (jedis.sismember(userKey, uid)) {
			System.out.println("不能重复的秒杀...");
			return false;
		}

		//3.是否初始化库存
		String qtStr = jedis.get(qtKey);
		if (qtStr == null || "".equals(qtStr.trim())) {
			System.out.println("未初始化库存...");
			jedis.close();
			return false;
		}

		//4.判断库存
		int qtCount = Integer.parseInt(qtStr);
		if (qtCount <= 0 ) {
			System.out.println("已经秒光了...");
			jedis.close();
			return false;
		}

		Transaction multi = jedis.multi();

		//5.减库存
		multi.decr(qtKey);

		//6.加人
		multi.sadd(userKey,uid);
		System.out.println("秒杀成功了...");
		jedis.close();
		return true;
	}

}
