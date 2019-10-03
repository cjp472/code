package com.lx.aop.代理.代理框架;//说明:


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 创建人:游林夕/2019/1/24 13 34
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
    //方法开始调用类
    Class<?>[] before_class() default {};
    //方法开始调用方法名
    String [] before_method() default {};
    //默认无参数,为true则取代理方法的参数
    boolean [] before_method_para() default {};

    Class<?>[] after_class() default {};
    String [] after_method() default {};
    boolean [] after_method_para() default {};
}

