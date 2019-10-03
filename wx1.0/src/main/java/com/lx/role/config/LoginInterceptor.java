package com.lx.role.config;//说明:

/**
 * 创建人:游林夕/2019/5/8 17 11
 */
import com.lx.role.dao.RedisUtil;
import com.lx.util.LX;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

//@Component
public class LoginInterceptor implements HandlerInterceptor {
    public static final String FILTER = ".*/((login)|(code)|(tcp)|(error)|(JEA)|(js)|(main)|(menu)|(wx)).*";
    public static final String HTMl = ".*\\.html.*";
    public static final String login_html = "/login.html";
    public static final String MENU_LIST = ".*/list/menu.*";
    @Resource(name="redisUtil")
    private RedisUtil redis;
    //这个方法是在访问接口之前执行的，我们只需要在这里写验证登陆状态的业务逻辑，就可以在用户调用指定接口之前验证登陆状态了
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath();
//        String ip = getIpAddress(request);
//        if (!ipLimiter(ip)) {//判断该ip是否多次登陆失败!
//            LX.exMsg("尝试次数太多!请1分钟后重试!");
//        }

        //拦截app
        if(path.matches(FILTER)||path.matches(".*/")){
            return true;
        }
        HttpSession session = request.getSession();
        //这里的User是登陆时放入session的
        String token = (String) session.getAttribute("token");
        if (LX.isEmpty(token)) token = request.getParameter("token");
        HashMap<String,String> user = null;
        if (LX.isNotEmpty(token)){
            user = redis.get(token,HashMap.class);
            return LX.isNotEmpty(user);
        }
        if (path.matches(HTMl))
            response.sendRedirect("/login.html");
        else{
            HttpServletResponse res = response;
            res.setContentType("application/json;charset=UTF-8");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"msg\":\"请重新登陆!\",\"success\":9}");
        }
        return false;
    }

    //说明:返回菜单列表
    /**{ ylx } 2019/5/9 15:13 */
    private void menu_list(HashMap<String,String> user){

    }
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
    //说明:对ip进行限流,防止攻击 一分钟5次
    /**{ ylx } 2019/4/25 15:11 */
    private boolean ipLimiter(String ip){
        return redis.acquirePromise(ip,1,5,60*1000);
    }
    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     *
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     *
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     *
     * 用户真实IP为： 192.168.1.110
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public void doGet(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();//返回请求行中的资源名称
        String url = request.getRequestURL().toString();//获得客户端发送请求的完整url
        String ip = request.getRemoteAddr();//返回发出请求的IP地址
        String params = request.getQueryString();//返回请求行中的参数部分
        String host=request.getRemoteHost();//返回发出请求的客户机的主机名
        int port =request.getRemotePort();//返回发出请求的客户机的端口号。
        System.out.println(ip);
        System.out.println(url);
        System.out.println(uri);
        System.out.println(params);
        System.out.println(host);
        System.out.println(port);
    }
}

