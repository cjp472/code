package com.lx.role.dao;//说明:

import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.function.Supplier;

/**
 * 创建人:游林夕/2019/4/24 14 36
 */
@Repository("redisUtil")
public class RedisUtil {
    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    private JedisPool jedisPool;

    public static void main(String [] args) throws Exception {
        new RedisUtil().put("app:gw:b",LX.toMap("{imgUrl='https://img.alicdn.com/i4/2446642632/O1CN01VacyYI1VJUxDD17v0_!!2446642632.jpg',tkl='￥9CN5YggtoCr￥',url='https://uland.taobao.com/coupon/edetail?e=ucHB47KHYxENfLV8niU3RwXoB%2BDaBK5LQS0Flu%2FfbSp4QsdWMikAalrisGmre1Id0BFAqRODu10Q7MGwcDbtmLbDPPBE192G%2BMUwzxYlSKECJ7VDxMdRd8KKEkQ8iLdyFZpNwmHwe%2FhGbpSJ%2BeSew6DMcCTQGbCRYnWgCosAWG1Or1nB9h97%2FxAgRNq79sNuMXWvr%2Bgbg0sM1L%2FmEra8u344d%2BzmctAY&&app_pvid=59590_11.131.126.77_414_1561815302869&ptl=floorId:2836;app_pvid:59590_11.131.126.77_414_1561815302869;tpp_pvid:100_11.14.233.176_89383_321561815302873835&xId=WKhIOUWXOyaWjnr83tiuQHOfrQsRowCXzxw68UVjK2Goz43CDAKimxEsvhtKNR5uzmjpYvE8xA5R6DEPg5sokQ&union_lens=lensId:0b837e4d_0bc9_16ba3724327_8f6a'}"));
    }
    //说明:从连接池获取链接
    /**{ ylx } 2019/4/24 14:52 */
    public Jedis getRedis() {
//        Jedis jedis = jedisPool.getResource();
        Jedis jedis = new Jedis("127.0.0.1");
        jedis.auth("123");
        LX.exObj(jedis,"不能获取到Jedis连接!");
        return jedis;
    }

    //说明:存储对象
    /**{ ylx } 2019/4/24 14:52 */
    public void put(String key, Object obj) {getRedis().set(key, LX.toJSONString(obj));}

    //说明:存储指定有效时间的key val
    /**{ ylx } 2019/4/24 16:56 */
    public void put(String key,Object val,int time){
        //nxxx： 只能取NX或者XX，如果取NX，则只有当key不存在是才进行set，如果取XX，则只有当key已经存在时才进行set
        //expx： 只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒。
        //time： 过期时间，单位是expx所代表的单位。
        del(key);
        getRedis().set(key,LX.toJSONString(val),"NX","EX",time);
    }

    //说明:删除
    /**{ ylx } 2019/4/24 17:01 */
    public void del(String key) {
        if (LX.isEmpty(key)) return;
            getRedis().del(key);
    }

    //说明:获取指定key存储的Map
    /**{ ylx } 2019/5/5 9:52 */
    public <T> List<T> getls(String pattern,Class<T> t) throws Exception {
        Set<String> keys = getRedis().keys(pattern);
        List<T> ls = new ArrayList<>();
        keys.forEach((k)->{ls.add(get(k,t));});
        return ls;
    }

    //说明:获取指定类型的对象
    /**{ ylx } 2019/4/24 17:01 */
    public <T> T get(String key,Class<T> t) {
        return LX.toObj(t,getRedis().get(key));
    }
    //说明:获取指定类型对象,不存在时存入,时间单位为秒
    /**{ ylx } 2019/4/25 11:18 */
    public <T> T get(String key, Class<T> t, Supplier<? extends T> other,int time) {
        return Optional.ofNullable(LX.toObj(t,getRedis().get(key))).orElseGet(()->{//缓存不存在该key
            T obj = null;
            synchronized (key){//加锁防止缓存雪崩
                obj = get(key,t);//重新重缓存获取
                if (LX.isEmpty(obj)){//如果还是不存在就去数据库查询
                    obj = other.get();
                    if (time <=0){
                        put(key,obj);//永久缓存
                    }else{
                        put(key,obj,time);//缓存指定时间
                    }
                }
            }
            return obj;
        });
    }
    //说明:获取字符串类型的值
    /**{ ylx } 2019/4/24 17:01 */
    public String get(String key) {
        return getRedis().get(key);
    }
    //说明:判断key是否存在
    /**{ ylx } 2019/4/25 14:59 */
    public boolean exists(String key){
        return getRedis().exists(key);
    }
    //说明:判断key是否存在
    /**{ ylx } 2019/4/25 14:59 */
    public boolean exists(String key,String field){
        return getRedis().hexists(key,field);
    }

    //说明:添加list数组
    /**{ ylx } 2019/5/5 9:45 */
    public void add(String key ,Object obj){
        getRedis().rpush(key,LX.toJSONString(obj));
    }
    public void add(String key ,Object obj,int len){
        getRedis().rpush(key,LX.toJSONString(obj));
        if (getRedis().llen(key)>len){
            getRedis().lpop(key);
        }
    }
    //说明:添加List
    /**{ ylx } 2019/5/5 11:02 */
    public void addAll(String key,List ls){
        List<String> list = new ArrayList<>();
        ls.forEach((obj)->{list.add(LX.toJSONString(obj));});
        getRedis().rpush(key, list.toArray(new String[ls.size()]));
    }
    //说明:修改指定下标的值
    /**{ ylx } 2019/5/5 11:02 */
    public void lset(String key,int index,Object obj){
        getRedis().lset(key,index,LX.toJSONString(obj));
    }

    //说明:获取指定key存储的list
    /**{ ylx } 2019/5/5 9:52 */
    public <T> List<T> getlist(String key,Class<T> t) throws Exception {
        return LX.toList(t,getlist(key));
    }
    public List<String> getlist(String key){
        return getRedis().lrange(key,0,-1);
    }
    //说明:删除指定下标的值(多个值相同时,会从左到右删除)
    /**{ ylx } 2019/5/5 11:34 */
    public void del(String key,int index){
        getRedis().lrem(key,1,getRedis().lindex(key,index));
    }

    //说明:获取指定长度的自增id
    /**{ ylx } 2019/5/5 15:44 */
    public String getId(String key,int length){
        return LX.right(getRedis().incr(key+"_incr").toString(),length,'0');
    }

    //说明:添加对象
    /**{ ylx } 2019/5/6 8:46 */
    public void save(String key,String id,Object obj){
        getRedis().hset(key,id,LX.toJSONString(obj));
    }
    //说明:删除对象
    /**{ ylx } 2019/5/6 8:50 */
    public void del(String key,String... id){
        getRedis().hdel(key,id);
    }
    //说明:获取所有对象
    /**{ ylx } 2019/5/6 8:52 */
    public <T> List<T> find(String key,Class<T> t){
        Map<String,String> map = getRedis().hgetAll(key);
        List<T> ls = new ArrayList<>();
        for (String str : map.values()){
            ls.add(LX.toObj(t,str));
        }
        return ls;
    }
    //说明:获取一个对象
    /**{ ylx } 2019/5/6 8:55 */
    public <T> T find(String key,Class<T> t,String id){
        return LX.toObj(t,getRedis().hget(key,id));
    }
    //使用redis 进行限流操作 start
    private String scriptShal;
    public void loadScript() {
        String script =" local function addToQueue(x,time)\n" +
                "     local count=0\n" +
                "     for i=1,x,1 do\n" +
                "         redis.call('lpush',KEYS[1],time)\n" +
                "         count=count+1\n" +
                "     end\n" +
                "     return count\n" +
                " end\n" +
                " local result=0\n" +
                " local timeBase = redis.call('lindex',KEYS[1], tonumber(ARGV[2])-tonumber(ARGV[1]))\n" +
                " if (timeBase == false) or (tonumber(ARGV[4]) - tonumber(timeBase)>tonumber(ARGV[3])) then\n" +
                "   result=result+addToQueue(tonumber(ARGV[1]),tonumber(ARGV[4]))\n" +
                " end\n" +
                " if (timeBase~=false) then\n" +
                "    redis.call('ltrim',KEYS[1],0,tonumber(ARGV[2])*3)\n" +
                " end\n" +
                " return result\n";
        this.scriptShal = getRedis().scriptLoad(script);
        log.info("滑动窗口流控脚本载入成功，sha1:{}"+this.scriptShal);
    }
    /**
     * 非阻塞请求
     * @param count 申请的数量
     * @param rateCount 限流数量
     * @param rateTime 限流时间 毫秒
     * @return 是否可以取到
     */
    public boolean acquirePromise(String redisKey,long count, long rateCount, long rateTime) {
        Assert.hasText(redisKey,"限流key不能为空");
        Assert.isTrue(count>0,"申请的数量不能小于0");
        Assert.isTrue(rateCount>0,"限流数量不能小于0");
        Assert.isTrue(rateTime>0,"限流时间不能小于0");
        if (scriptShal == null){
            synchronized (this){
                if (scriptShal == null){
                    loadScript();//注入脚本
                }
            }
        }
        List<String> keys = new ArrayList<>();
        keys.add(redisKey);//队列名
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(count)); //申请发送的数量 1
        values.add(String.valueOf(rateCount));//阀值数量 2
        values.add(String.valueOf(rateTime));//阀值时间（毫秒）3
        values.add(String.valueOf(System.currentTimeMillis()));//申请的时间4
        Jedis jedis = getRedis();
        Object evalResult = jedis.evalsha(scriptShal, keys, values);
        return Long.parseLong(evalResult.toString())!=0;
    }

    //使用redis 进行限流操作 end
}
