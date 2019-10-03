package com.lx.util.doc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//@Retention注解指定注解可以保留多久
@Retention(RetentionPolicy.RUNTIME)//启动服务之后还可以用
//@Target注解指定注解能修饰的目标(只能是方法)
//@Target(ElementType.METHOD)
/**
 * 用于生成接口Api文档
 * @author 游林夕
 *
 */
public @interface Doc {
	/**
	 * 	类的名称 .方法的名字
	 */
    String name();

    /**
     * 	接口名
     */
    String method();
    /**
     * 	入参	实例: RegID=挂号ID=特殊情况说明
     */
    String in() default "";
    /**
     * 	出参	实例: RegID=挂号ID=特殊情况说明
     */
    String out() default "";
    /**
     * 	当前方法的说明 
     */
    String msg() default "";
    /**
     * 文档类型
     */
    String type();
    /**作者*/
    String auth();
    /**开发时间*/
    String day();

}
