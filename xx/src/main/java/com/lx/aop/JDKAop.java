package com.lx.aop;//说明:

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 创建人:游林夕/2019/4/3 15 57
 */
public class JDKAop{
    public static void main(String [] args){
        JDKAop j = new JDKAop();
        JDKAop a = (JDKAop) Proxy.newProxyInstance(j.getClass().getClassLoader(), j.getClass().getInterfaces(),new InvocationHandler(){
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("开始");
                return method.invoke(j,args);
            }
        });
        a.add();
    }


    public void add() {
        System.out.println("啊啊啊");
    }
}
interface A{
    void add();
}