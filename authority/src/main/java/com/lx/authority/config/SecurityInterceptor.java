package com.lx.authority.config;

import com.lx.authority.dao.RedisUtil;
import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/**
 * @author blueriver
 * @description 权限拦截器
 * @date 2017/11/17
 * @since 1.0
 */
public class SecurityInterceptor implements HandlerInterceptor {
    public static final String HTMl = ".*/.html.*";
    public static final String SYS = ".*/sys/.*";
    @Resource(name="redisUtil")
    private RedisUtil redis;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Authority authority = Optional.ofNullable(handlerMethod.getMethod().getAnnotation(Authority.class))
                    .orElse(handlerMethod.getMethod().getDeclaringClass().getAnnotation(Authority.class));
            // 如果标记了注解，则判断权限
            if (authority!=null && authority.value()) {
                try{
                    if (OS.setUser(request)){//没有设置成功
                        //是否检查接口权限
                        if (!authority.method()) return true;//不用直接返回true
                        try {
                            return OS.checkMethod(request,authority);
                        }catch (Exception e){
                            response.setContentType("application/json;charset=UTF-8");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(LX.toJSONString(new OS.Page(e.getMessage())));
                            return false;
                        }
                    }
                }catch (Exception e){
                }
                response.setContentType("application/json;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(OS.Page.toLogin());
                return false;

            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        OS.remove();
    }
}
