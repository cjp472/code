package com.lx.authority.controller;//说明:

import com.lx.authority.config.OS;
import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 创建人:游林夕/2019/5/5 11 10 返回值出现异常时
 */
@Controller
public class WebExceptionResolver implements HandlerExceptionResolver {
    private Logger log = LoggerFactory.getLogger(WebExceptionResolver.class);
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        log.error("请求{}发生异常！",e);
        ModelAndView mv = new ModelAndView();
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setCharacterEncoding("UTF-8");
        try{
            httpServletResponse.getWriter().write(LX.toJSONString(new OS.Page(e.getCause()==null?e.getMessage():e.getCause().getMessage())));
        }catch(Exception ex){
            log.error("WebExceptionResolver处理异常",ex);
        }
        // 需要返回空的ModelAndView以阻止异常继续被其它处理器捕获
        return mv;
    }
}
