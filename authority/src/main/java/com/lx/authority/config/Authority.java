package com.lx.authority.config;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.*;
import java.util.function.Predicate;

/**
 * @author blueriver
 * @description 与拦截器结合使用 验证权限
 * @date 2017/11/17
 * @since 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Authority {
    /** 验证当前方法*/
    boolean value() default true;
    /** 验证接口 */
    boolean method() default false;
    /**验证接口的类和方法*/
    Class<? extends Predicate> classAndMethod() default Predicate.class;
}