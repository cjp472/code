package com.lx.aop.代理.静态代理;

/**
 * @Author:游林夕
 * @Date: 2018/10/6 14:36
 * Description:
 */
public class Factory {
    public static Dept getDeptInstance(){
        return new DeptProxy(new DeptImpl());
    }
}
