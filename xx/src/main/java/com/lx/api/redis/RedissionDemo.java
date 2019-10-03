package com.lx.api.redis;//说明:

import org.apache.log4j.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 创建人:游林夕/2019/4/17 09 19
 */
public class RedissionDemo {
    public static void main(String[] args) throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        RedissonClient redisson = Redisson.create(config);
//        System.out.println(redisson.getConfig().toJSON().toString());
//        RBucket<HashMap> bucket = redisson.getBucket("anyObject");
//        bucket.set(new HashMap());
//        HashMap obj = bucket.get();
//        System.out.println(obj);

//        RMap<String, String> map = redisson.getMap("anyMap");
//        map.put("aa","1");
//        map.putIfAbsent("aa","2");
//        System.out.println(map.get("aa"));

//        RRemoteService remoteService = redisson.getRemoteService();
//        remoteService.register(A.class,new B());
//        remoteService.register(A.class,new B(),12);//支持多个线程同时调用

    }

}
class RedisUtils {
    private static RedisUtils redisUtils;
    private RedisUtils(){}
    /**
     * 提供单例模式
     * @return
     */
    public static RedisUtils getInstance(){
        if(redisUtils==null)
            synchronized (RedisUtils.class) {
                if(redisUtils==null) redisUtils=new RedisUtils();
            }
        return redisUtils;
    }


    /**
     * 使用config创建Redisson
     * Redisson是用于连接Redis Server的基础类
     * @param config
     * @return
     */
    public RedissonClient getRedisson(Config config){
        RedissonClient redisson=Redisson.create(config);
        return redisson;
    }

    /**
     * 使用ip地址和端口创建Redisson
     * @param ip
     * @param port
     * @return
     */
    public RedissonClient getRedisson(String ip,String port){
        Config config=new Config();
        config.useSingleServer().setAddress(ip+":"+port);
        RedissonClient redisson=Redisson.create(config);
        return redisson;
    }

    /**
     * 关闭Redisson客户端连接
     * @param redisson
     */
    public void closeRedisson(RedissonClient redisson){
        redisson.shutdown();
    }

    /**
     * 获取字符串对象
     * @param redisson
     * @param objectName
     * @return
     */
    public <T> RBucket<T> getRBucket(RedissonClient redisson,String objectName){
        RBucket<T> bucket=redisson.getBucket(objectName);
        return bucket;
    }

    /**
     * 获取Map对象
     * @param redisson
     * @param objectName
     * @return
     */
    public <K,V> RMap<K, V> getRMap(RedissonClient redisson,String objectName){
        RMap<K, V> map=redisson.getMap(objectName);
        return map;
    }

    /**
     * 获取有序集合
     * @param redisson
     * @param objectName
     * @return
     */
    public <V> RSortedSet<V> getRSortedSet(RedissonClient redisson,String objectName){
        RSortedSet<V> sortedSet=redisson.getSortedSet(objectName);
        return sortedSet;
    }

    /**
     * 获取集合
     * @param redisson
     * @param objectName
     * @return
     */
    public <V> RSet<V> getRSet(RedissonClient redisson,String objectName){
        RSet<V> rSet=redisson.getSet(objectName);
        return rSet;
    }

    /**
     * 获取列表
     * @param redisson
     * @param objectName
     * @return
     */
    public <V> RList<V> getRList(RedissonClient redisson,String objectName){
        RList<V> rList=redisson.getList(objectName);
        return rList;
    }

    /**
     * 获取队列
     * @param redisson
     * @param objectName
     * @return
     */
    public <V> RQueue<V> getRQueue(RedissonClient redisson,String objectName){
        RQueue<V> rQueue=redisson.getQueue(objectName);
        return rQueue;
    }

    /**
     * 获取双端队列
     * @param redisson
     * @param objectName
     * @return
     */
    public <V> RDeque<V> getRDeque(RedissonClient redisson,String objectName){
        RDeque<V> rDeque=redisson.getDeque(objectName);
        return rDeque;
    }

    /**
     * 此方法不可用在Redisson 1.2 中
     * 在1.2.2版本中 可用
     * @param redisson
     * @param objectName
     * @return
     */
     public <V> RBlockingQueue<V> getRBlockingQueue(RedissonClient redisson,String objectName){
     RBlockingQueue rb=redisson.getBlockingQueue(objectName);
     return rb;
     }

    /**
     * 获取锁
     * @param redisson
     * @param objectName
     * @return
     */
    public RLock getRLock(RedissonClient redisson,String objectName){
        RLock rLock=redisson.getLock(objectName);
        return rLock;
    }

    /**
     * 获取原子数
     * @param redisson
     * @param objectName
     * @return
     */
    public RAtomicLong getRAtomicLong(RedissonClient redisson,String objectName){
        RAtomicLong rAtomicLong=redisson.getAtomicLong(objectName);
        return rAtomicLong;
    }

    /**
     * 获取记数锁
     * @param redisson
     * @param objectName
     * @return
     */
    public RCountDownLatch getRCountDownLatch(RedissonClient redisson,String objectName){
        RCountDownLatch rCountDownLatch=redisson.getCountDownLatch(objectName);
        return rCountDownLatch;
    }

    /**
     * 获取消息的Topic
     * @param redisson
     * @param objectName
     * @return
     */
    public <M> RTopic<M> getRTopic(RedissonClient redisson,String objectName){
        RTopic<M> rTopic=redisson.getTopic(objectName);
        return rTopic;
    }


}
