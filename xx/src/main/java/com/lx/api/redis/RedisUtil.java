package com.lx.api.redis;//说明:

import com.lx.util.LX;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 创建人:游林夕/2019/4/8 08 48
 */
public class RedisUtil {
    private static Jedis jedis = new Jedis("220.194.101.89");

    public static void main(String [] args){
        jedis.auth("123");
    }
    @Test
    public void id(){
        jedis.auth("123");
        System.out.println(jedis.incr("id"));//获取自增主键

    }

    //说明:通过原子性的设置值,如果返回成功则获取到锁
    /**创建人:游林夕 -- 2019/3/9 13:26 --*/
    public boolean lock(String key,String val,long time){
        //nxxx： 只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
        //expx： 只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒。
        //time： 过期时间，单位是expx所代表的单位。
        return "ok".equalsIgnoreCase(jedis.set(key,val,"NX","PX",time));
    }
    //说明:不停尝试拿到锁,直到返回
    /**创建人:游林夕 -- 2019/3/9 13:26 --*/
    public void tryLock(String key,String val,long time) throws InterruptedException {
        while (!lock(key,val,time)){
            Thread.sleep(100);
            tryLock(key,val,time);
        }
    }
    public void unlock(String key,String val){
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(val));
    }
    //说明:redis使用map方式存取数据
    /**创建人:游林夕 -- 2019/3/8 16:29 --*/
    @Test
    public void testMap(){
        //nxxx： 只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
        //expx： 只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒。
        //time： 过期时间，单位是expx所代表的单位。
        jedis.set("key","val","NX","PX",1000);



        jedis.set("key","val");
        System.out.println(jedis.get("key"));
        System.out.println(jedis.del("key"));
        System.out.println(jedis.get("key"));

        System.out.println(jedis.hset("dd","aa","aa"));//新增dd下的aa是值 hash值
        System.out.println(jedis.hget("dd","aa"));//取出dd下的aa的值

        close();
    }
    //说明:redis使用list方式存取数据
    /**创建人:游林夕 -- 2019/3/8 16:30 --*/
    @Test
    public void testList(){
        jedis.lpush("list","l1");
        jedis.lpush("list","l2","l3");//从前向后插入
        jedis.rpush("list","r1","r2");//从后向前插入
        List<String> list = jedis.lrange("list",0,1);//取出下标0到1之间所有数据,含头且含尾
        list = jedis.lrange("list",0,-1);//取出所有数据
        System.out.println(list);
        System.out.println(LX.format("获取指定位置的元素的值{0}",jedis.lindex("list",0)));
        System.out.println(LX.format("替换指定位置的元素的值:{0}",jedis.lset("list",0,"哈哈")));
        System.out.println(LX.format("保留指定位置的元素:{0}",jedis.ltrim("list",0,2)));

        System.out.println(LX.format("等待1000阻塞从左侧取出元素,",jedis.blpop(1000,"list")));

        System.out.println(LX.format("弹出第一个元素:{0}",jedis.lpop("list")));
        System.out.println(LX.format("弹出最后一个元素:{0}",jedis.rpop("list")));
        System.out.println(LX.format("查询链表的的剩余数量:{0}",jedis.llen("list")));
        System.out.println(LX.format("删除从前到后指定个数的指定值(返回实际删除个数):{0}",jedis.lrem("list",3,"list1")));

        System.out.println(LX.format("将链表1的最后一次元素放在链表2的头部{0},list1:{1}",jedis.rpoplpush("list","list1"),jedis.lrange("list1",0,-1)));
        System.out.println(LX.format("将链表1的最后一次元素放在链表1的头部{0},list:{1}",jedis.rpoplpush("list1","list1"),jedis.lrange("list",0,-1)));


        close();
    }

    //说明:
    /**创建人:游林夕 -- 2019/3/8 17:21 --*/
    @Test
    public void testSet(){
        jedis.sadd("mySet","a","b","c"); //点赞
        System.out.println(LX.format("获取set集合的大小:{0}",jedis.scard("mySet")));
        System.out.println(LX.format("获取set集合所有值:{0}",jedis.smembers("mySet"))); //点赞列表
        jedis.srem("mySet");//删除 取消点赞
        jedis.sismember("mySet","d");//d是否存在
        jedis.spop("mySet",2);//从集合中选{count}元素 删除
        jedis.srandmember("mySet",2);//从集合中选{count}元素 不删除


        jedis.sinter("key1","key2");//交集
        jedis.sunion("key1","key2");//并集
        jedis.sdiff("key1","key2");//差集




        //热点事件排行榜  自动补全
        jedis.zadd("ke",22,"aa");//有序集合
        jedis.zscore("ke","aa");//
        jedis.zincrby("ke",1,"aa");//
        jedis.zrevrange("ke",10,20);//
        jedis.zunionstore("ke","ke1");//对多个集合取并集

        close();
    }

    //说明:获取所有的东西
    /**创建人:游林夕 -- 2019/3/8 17:13 --*/
    @Test
    public void testAll(){
        Set<String> keys = jedis.keys("*");
        System.out.println(keys);
        close();
    }

    @Test
    public void transaction() throws IOException {
        jedis.lpush("ls","添加的元素");
        System.out.println(LX.format("获取插入的信息:{0}",jedis.lrange("ls",0,-1)));
        Transaction transaction = jedis.multi();
        try {
            transaction.lpush("ls","a","b");
            transaction.rpush("ls",6/0+"");
            transaction.lpush("ls","aaa");
            List<Object> l = transaction.exec();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            transaction.close();
        }
        System.out.println(LX.format("获取插入的信息:{0}",jedis.lrange("ls",0,-1)));
        close();
    }

    public void close(){
        jedis.flushAll();//清除所有
        jedis.close();
    }
}
