package com.lx.aop.代理.代理框架;//说明:

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * 创建人:游林夕/2019/1/24 17 18
 */
public abstract class ProxyAbs implements MethodInterceptor {

    private Class<?> cls;
    public abstract  void before(final Class<?> cls, Object[] objects);
    public abstract  void after(Object o);
    public Object exception(Exception e) throws Exception {throw e;};
    public <T> T newInstance(Class<T> cls){
        Enhancer e = new Enhancer();//代理工具类
        e.setSuperclass(this.cls = cls);//设置父类
        e.setCallback(this);//设置代理对象
        return (T)e.create();
    }
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Object obj = null;
        try{
            before(cls,objects);
            obj =  methodProxy.invokeSuper(o,objects);
            after(obj);
        }catch (Exception e){
            obj = exception(e);
        }
        return obj;
    }
}
