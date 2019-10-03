package com.lx.aop.动态代理;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.InvokeHandler;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 全局动态代理
 * @Author:游林夕
 * @Date: 2018/10/6 14:37
 * Description:
 */
public class MyProxy implements InvocationHandler {
    private Object obj ;
    public Object  bind(Object obj){
        this.obj = obj;
        return   Proxy.newProxyInstance(obj.getClass().getClassLoader(),obj.getClass().getInterfaces(),this);
    }
    public  void before(){
        System.out.println("执行开始操作");
    }
    public void end(){
        System.out.println("执行结束操作");
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        before();
        Object o1 = method.invoke(obj , objects);
        end();
        return o1;
    }
}
