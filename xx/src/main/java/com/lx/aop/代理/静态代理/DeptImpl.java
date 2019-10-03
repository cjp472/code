package com.lx.aop.代理.静态代理;

/**
 * @Author:游林夕
 * @Date: 2018/10/6 14:28
 * Description:
 */
public class DeptImpl implements Dept{
    @Override
    public void insert() {
        System.out.println("执行具体操作");
    }
}
