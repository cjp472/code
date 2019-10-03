package com.lx.aop.代理.代理框架;//说明:

import com.lx.util.LX;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 创建人:游林夕/2019/1/24 13 45
 */
public class ProxyFactory {

    public static void main(String [] args) throws Exception {
//        newInstance(ProxyFactory.class).say("adas");
        new LogProxy().newInstance(ProxyFactory.class).say("大大");
    }

//    @Proxy(before_class = AA.class ,before_method ="before" ,before_method_para = false)
    public Object say(String str) throws Exception {
        System.out.println("我是具体方法");
        LX.exMsg("错误!");
        return "大大大声点";
    }
    //说明:动态代理类,传入代理类
    /**创建人:游林夕 -- 2019/1/24 13:45 --*/
    private static <T> T newInstance(Class<T> cls, Callback callback){
        Enhancer e = new Enhancer();//代理工具类
        e.setSuperclass(cls);//设置父类
        //设置代理对象
        e.setCallback(callback);
        return (T)e.create();
    }
    //说明:动态代理类
    /**创建人:游林夕 -- 2019/1/24 13:45 --*/
    private static <T> T newInstance( Class<T> cls){
        Enhancer e = new Enhancer();//代理工具类
        e.setSuperclass(cls);//设置父类
        //设置代理对象
        e.setCallback(new MethodInterceptor(){

            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Proxy proxy = method.getAnnotation(Proxy.class);
                if (proxy.before_class().length != proxy.before_method().length || proxy.before_class().length != proxy.before_method_para().length)
                    LX.exMsg(LX.format("请在{0}类的{1}方法中配置正确的before数量",cls.getSimpleName(),method.getName()));
                if (proxy.after_class().length != proxy.after_method().length || proxy.after_class().length != proxy.after_method_para().length)
                    LX.exMsg(LX.format("请在{0}类的{1}方法中配置正确的after数量",cls.getSimpleName(),method.getName()));
                if (proxy != null && proxy.before_class().length>0){
                    for (int i = 0;i<proxy.before_class().length;i++){
                        Method m = getMethod(proxy.before_class()[i],proxy.before_method()[i],proxy.before_method_para()[i],objects);
                        invokeMethod(proxy.before_class()[i].newInstance(),m,proxy.before_method_para()[i],objects);
                    }

                }
                //调用实际方法
                Object o1 = methodProxy.invokeSuper(o , objects);

                if (proxy != null && proxy.after_class().length>0){
                    for (int i = 0;i<proxy.after_class().length;i++) {
                        Method m = getMethod(proxy.after_class()[i],proxy.after_method()[i],proxy.after_method_para()[i],objects);
                        invokeMethod(proxy.after_class()[i].newInstance(),m,proxy.after_method_para()[i],objects);
                    }
                }
                return o1;
            }
        });//设置代理对象
        return (T)e.create();//创建代理对象
    }
    private static Method getMethod(Class<?> cls, String method, boolean boo, Object[] objects) throws NoSuchMethodException {
        Method m = null;
        if (boo && objects.length>0){
            Class<?>[] objectClasses = new Class<?>[objects.length];
            for (int j=objects.length-1;j>=0;j--){
                objectClasses[j] = objects[j].getClass();
            }
            m = cls.getDeclaredMethod(method,objectClasses);
        }else {
            m = cls.getDeclaredMethod(method);
        }
        return m;
    }
    private static void invokeMethod(Object obj, Method m,boolean boo,Object...args) throws InvocationTargetException, IllegalAccessException {
        if (m != null){
            if (m.isAccessible()){
                if (boo){
                    m.invoke(obj,args);
                }else {
                    m.invoke(obj);
                }
            }else{
                m.setAccessible(true);
                if (boo){
                    m.invoke(obj,args);
                }else {
                    m.invoke(obj);
                }
                m.setAccessible(false);
            }
        }
    }
}
