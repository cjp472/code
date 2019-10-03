package com.lx.aop.动态代理;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

class Book{
    public  void buy(){
        System.out.println("我买了一本书");
    }
}
/**
 * @Author:游林夕
 * @Date: 2018/10/6 15:1
 * Description:
 */
public class BookProxy implements MethodInterceptor  {

    private Object target;
    public BookProxy(Object o){
        target = o;
    }
    public void pay(){
        System.out.println("收银50");
    }
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        pay();
        return method.invoke(target,objects);
    }
}
