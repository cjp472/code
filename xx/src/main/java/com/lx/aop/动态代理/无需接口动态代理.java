package com.lx.aop.动态代理;

import net.sf.cglib.proxy.Enhancer;

/**
 * @Author:游林夕
 * @Date: 2018/10/6 15:21
 * Description:
 */
public class 无需接口动态代理 {
    public static void main(String[] args) {
        Book b = new Book();
        Enhancer e = new Enhancer();//代理工具类
        e.setSuperclass(Book.class);//设置父类
        e.setCallback(new BookProxy(b));//设置代理对象
        Book p = (Book) e.create();//创建代理对象
        p.buy();
    }
}
