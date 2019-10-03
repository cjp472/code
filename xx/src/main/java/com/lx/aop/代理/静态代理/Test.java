package com.lx.aop.代理.静态代理;

/**
 * @Author:游林夕
 * @Date: 2018/10/6 14:29
 * Description:
 */
public class Test {
    public static void main(String[] args) {
        Dept d = Factory.getDeptInstance();
        d.insert();
    }

}
