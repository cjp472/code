package com.lx.core;//说明:

import com.lx.core.util.ScanPackage;
import com.lx.util.LX;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * 创建人:游林夕/2019/6/10 10 27
 * 鸿蒙 一切的开始
 */
public class HungMeng {
    //用来存储bean的容器
    private Map<String,Object> beans;
    public static void main(String [] args) throws IOException {
        HungMeng.start();


    }
    //程序主入口 入参为扫描的包
    public static void start(String...scanPage) throws IOException {
        if (LX.isEmpty(scanPage)){//获取当前类的包
            String page = Thread.currentThread().getStackTrace()[2].getClassName();//获取调用当前方法的类
            page = page.substring(0,page.lastIndexOf(".")); //获取包名
            scanPage = new String[]{page};
        }
        //1.将需要扫描的包加入容器
        beanRegister(scanPage);
        //2.注入
        injection();
    }
    //说明:扫描指定的包
    /**{ ylx } 2019/6/10 10:33 */
    private static void beanRegister(String... scanPage) throws IOException {
        List<Class<?>> ls =  ScanPackage.scan(scanPage);

        if (LX.isEmpty(ls))return;
    }
    //说明:将容器中的对象进行注入
    /**{ ylx } 2019/6/10 10:34 */
    private static void injection() {
    }

    //说明:通用注解 表示当前对象需要注入
    /**{ ylx } 2019/6/10 10:44 */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE,ElementType.FIELD})
    @interface LXBean{
        String value() default "";
    }
}
