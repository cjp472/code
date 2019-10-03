package com.lx.aop.代理.静态代理;

/**
 * @Author:游林夕
 * @Date: 2018/10/6 14:30
 * Description:
 */
public class DeptProxy implements Dept {
    private Dept dept;
    public DeptProxy(Dept dept){
        this.dept = dept;
    }
    public  void before(){
        System.out.println("执行开始操作");
    }
    public void end(){
        System.out.println("执行结束操作");
    }
    @Override
    public void insert() {
        before();
        dept.insert();
        end();
    }
}
