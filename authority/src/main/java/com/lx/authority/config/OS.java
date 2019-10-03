package com.lx.authority.config;//说明:

/**
 * 创建人:游林夕/2019/4/28 09 04
 */

import com.lx.authority.dao.RedisUtil;
import com.lx.entity.Var;
import com.lx.util.LX;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class OS implements ApplicationContextAware,EnvironmentAware {
    public static final String USERNAME = "id";
    public static final String PASSWORD = "password";
    //存入redis
    private static final String USER_TOKEN = "system:login:token:";
    private static final String CUSTOM_USER = "system:login:custom:";
    //token超时时间
    private static int token_timeout;
    //尝试登录错误次数
    private static int minuteLimit;
    //单点登录
    private static String server_login_single;
    private static final ThreadLocal<Map<String, Object>> tl = new ThreadLocal() {
        protected Map<String, Object> initialValue() {
            return new HashMap(4);
        }
    };
    private static RedisUtil redisUtil;
    private static Environment environment;

    /** 获取缓存中的对象*/
    static HashMap getUserByToken(String token){
        return redisUtil.get("system:login:token:"+token,HashMap.class);
    }
    /** 从缓存中移除登录信息*/
    public static void logout(HttpServletRequest request){
        removeUser(request);
    }
    /**
     * username , 用户对应的库里的信息
     * func 返回true则登录成功 false 登录失败
     */
    public static String login(HttpServletRequest request,String username,Object custom, Function<HashMap,Boolean> func){
        String token = LX.uuid();
        long ipLimit = getIpLimit(request,()->{//
            Var user = null;
            if ("admin".equals(username)){
                user = LX.toMap("{{0}='admin',{1}='{2}',menus='{3}',btns='{4}'}",USERNAME,PASSWORD,"123456","#menus#","#btns#");
            }else{
                user = redisUtil.find("system:user",Var.class,username);
                LX.exObj(user,"没有找到用户信息!");
                Var role = redisUtil.find("system:role",Var.class,user.getStr("role"));
                LX.exObj(role,"没有绑定角色权限!");
                user.put("menus",role.getStr("menus"));
                user.put("btns",role.getStr("btns"));
            }

            if (func.apply(user)){
                saveUser(request,token,user);
                if (LX.isNotEmpty(custom)){
                    redisUtil.put(CUSTOM_USER+username,LX.toJSONString(custom));//缓存
                }
            }else{
                LX.exMsg("登陆验证失败!");
            }
            return true;
        });
        if (ipLimit>0) LX.exMsg("请{0}秒后重试!",ipLimit);
        return token;
    }
    private static void saveUser(HttpServletRequest request, String token, Var user){
        user.put("token",token);
        //登陆成功,返回登陆TOKEN
        if ("1".equals(server_login_single)){
            //单点登录
            redisUtil.del(redisUtil.get("system:login:user_token:"+user.get(USERNAME)));//删除之前的token
            redisUtil.put("system:login:user_token:"+user.get(USERNAME),token,token_timeout);//将当前用户的token记住
        }
        redisUtil.put(USER_TOKEN+token,user,token_timeout);//缓存
        request.getSession().setAttribute("token",token);
        request.getSession().setMaxInactiveInterval(token_timeout);
    }
    /** 移除用户*/
    private static void removeUser(HttpServletRequest request){
        Var user = getUser();
        if (LX.isEmpty(user)) return;
        String token = user.getStr("token");
        //登陆成功,返回登陆TOKEN
        if ("1".equals(server_login_single)){//单点登录
            redisUtil.del(redisUtil.get("system:login:user_token:"+user.get(USERNAME)));//删除之前的token
        }
        redisUtil.del(USER_TOKEN+token);//缓存
        request.getSession().removeAttribute("token");
    }
    //设置用户
    static boolean setUser(HttpServletRequest request){
        //尝试从Session中获取token
        String token = (String) request.getSession().getAttribute("token");
        //从参数中获取
        if (LX.isEmpty(token)) token = request.getParameter("token");
        if (LX.isNotEmpty(token)){
            Var user = redisUtil.get(USER_TOKEN+token,Var.class);
            if (LX.isNotEmpty(user)){
                saveUser(request,token,user);//重新保存
                put(USER_TOKEN,user);
                return true;
            }
        }
        return false;
    }

    /** 验证方法 */
    static boolean checkMethod(HttpServletRequest request,Authority authority) throws Exception {
        //注解写入指定验证接方法
        if(!authority.classAndMethod().isInterface()){
            if (authority.classAndMethod().newInstance().test(request)){
                return true;
            }
        }else{
            Var var = getParameterMap(request);
            Var user = getUser();
            String btns = user.getStr("btns");
            if (LX.isNotEmpty(btns) &&("#btns#".equals(btns) || new HashSet<>(Arrays.asList(btns.split(","))).contains(var.get("method")))){
                return true;
            }
        }
        return LX.exMsg("没有该接口权限");
    }

    /**返回用户*/
    public static Var getUser(){
        return get(USER_TOKEN);
    }
    public static <T>T getCustom(Class<T> tClass){
        return redisUtil.get(CUSTOM_USER+getUser().getStr(USERNAME),tClass);
    }

    public static  <T>T get(String key){
        return (T) tl.get().get(key);
    }
    public static void put(String key,Object obj){
        tl.get().put(key,obj);
    }
    public static void remove(){
        tl.remove();
    }
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        redisUtil = applicationContext.getBean(RedisUtil.class);
    }

    /**
     * 通过name获取 Bean.
     */
    public static Object getBean(String name){
        return applicationContext.getBean(name);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.token_timeout = Integer.parseInt(Optional.ofNullable(getProperty("server.token.timeout")).orElse(60*60+""));
        this.server_login_single = Optional.ofNullable(getProperty("server.login.single")).orElse("1");
        this.minuteLimit = Integer.parseInt(Optional.ofNullable(getProperty("server.login.minuteLimit")).orElse("5"));
    }
    public static String getProperty(String key){
        return environment.getProperty(key);
    }
    /** 根据key 获取方法*/
    public static Var getMethod(String method){
        LX.exObj(method,"方法名不能为空!");
        return redisUtil.find("system:service",Var.class,method);
    }
    /** 调用方法*/
    public static Object invokeMethod(Map map) throws Exception {
        LX.exMap(map,"method");
        HashMap<String,String> sc = redisUtil.find("system:service",HashMap.class,map.get("method").toString());
        LX.exObj(sc,"接口服务配置不存在!");
        Method m = null;
        Object cls = null;
        try {
            cls = getBean(sc.get("cls"));
            m = cls.getClass().getDeclaredMethod(sc.get("method"),Map.class);
        } catch (Exception e) {
            LX.exMsg("不能查找到方法==>"+sc.get("cls")+"."+sc.get("method"));
        }
        return m.invoke(cls,map);
    }

    public static Var getParameterMap(HttpServletRequest request) {
        // 参数Map
        Map<?, ?> properties = request.getParameterMap();
        // 返回值Map
        Var returnMap = new Var();
        Iterator<?> entries = properties.entrySet().iterator();

        Map.Entry<String, Object> entry;
        String name = "";
        String value = "";
        Object valueObj =null;
        while (entries.hasNext()) {
            entry = (Map.Entry<String, Object>) entries.next();
            name = (String) entry.getKey();
            valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }
    public static long getIpLimit(HttpServletRequest req, Supplier<Boolean> supp){
        return redisUtil.minuteLimit(getIpAddress(req),minuteLimit,supp);
    }
    /** 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址, */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)){
            return "127.0.0.1";
        }
        return ip;
    }



    public static class Page {
        private String msg;
        private int success = 1;
        private long count = 0;
        private List rows;
        private Map entity;

        public Page(){}
        public Page(Map map){
            this.entity = map;
        }
        public Page(List data){
            if (data == null) return;
            this.rows = data;
            this.count = data.size();
        }
        public Page(List rows, long count){
            this.rows = rows;
            this.count = count;
        }
        public Page(String msg){
            this.msg = msg;
            this.success = 0;
        }
        public Page(String msg,int success){
            this.msg = msg;
            this.success = success;
        }
        public static String toLogin(){
            return LX.toJSONString(new OS.Page("请重新登陆",9));
        }
        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "Page{" +
                    "msg='" + msg + '\'' +
                    ", success=" + success +
                    ", count=" + count +
                    ", rows=" + rows +
                    ", entity=" + entity +
                    '}';
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }

        public List getRows() {
            return rows;
        }

        public void setRows(List rows) {
            this.rows = rows;
        }

        public Map getEntity() {
            return entity;
        }

        public void setEntity(Map entity) {
            this.entity = entity;
        }
    }
}

