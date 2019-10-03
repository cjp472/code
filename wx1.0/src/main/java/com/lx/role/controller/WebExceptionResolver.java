package com.lx.role.controller;//说明:

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 创建人:游林夕/2019/5/5 11 10
 */
@Controller
public class WebExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ModelAndView mv = new ModelAndView();
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");
        try{
            httpServletResponse.getWriter().write("{\"msg\":\""+Optional.ofNullable(e.getCause()).orElse(e).getMessage()+"\",\"success\":0}");
        }catch(Exception ex){
        }
        // 需要返回空的ModelAndView以阻止异常继续被其它处理器捕获
        return mv;
    }
}
