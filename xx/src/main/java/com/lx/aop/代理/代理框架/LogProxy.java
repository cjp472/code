package com.lx.aop.代理.代理框架;//说明:

import java.util.Arrays;

/**
 * 创建人:游林夕/2019/1/24 17 24
 */
public class LogProxy extends ProxyAbs {
    @Override
    public void before(Class<?> o, Object[] objects) {
        System.out.println("开始记录日志:"+o.getName()+"参数:"+Arrays.asList(objects));
    }

    @Override
    public void after(Object o) {
        System.out.println("结束记录日志>>>返回值:"+o);
    }

    @Override
    public Object exception(Exception e) throws Exception {
        System.out.println("出现异常:"+e.getMessage());
        throw e;
    }
}
