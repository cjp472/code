package com.lx.authority.controller;

import com.lx.authority.config.Authority;
import com.lx.authority.config.OS;
import com.lx.authority.dao.RedisUtil;
import com.lx.entity.Var;
import com.lx.util.LX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by 游林夕 on 2019/8/23.
 */
@Controller
@Authority()
public class LoginController {
    @Autowired
    private RedisUtil redis;

    @Authority(false)
    @RequestMapping("/login")
    @ResponseBody
    public Map getUsers(@RequestParam(required = false) Map map, HttpServletRequest request) throws Exception {
        LX.exMap(map,"loginname,password,code");
        if (!map.get("code").toString().toLowerCase().equals(LX.str(request.getSession().getAttribute("code")).toLowerCase())) return LX.toMap("{result='codeerror'}");;
        //从缓存中查找用户信息
        //登录
        String token = OS.login(request,map.get("loginname").toString(),null,(user)->{
            return user.get(OS.USERNAME).equals(map.get("loginname"))&&user.get(OS.PASSWORD).equals(map.get("password"));
        });
        return LX.toMap("{result='success',token='{0}'}",token);
    }
    //说明:退出登录
    /**{ ylx } 2019/9/11 11:32 */
    @RequestMapping("/logout")
    public String logout(@RequestParam(required = false) Map map,HttpServletRequest request){
        OS.logout(request);
        return LX.isEmpty(map.get("path"))?"redirect:/index.html":"redirect:/sys/index.html";
    }
    //用户菜单
    @RequestMapping("/menu")
    @ResponseBody
    public Object list_menu(HttpServletRequest request) throws Exception {
        List<HashMap> ls = null;
        //获取当前用户信息
        Var user = OS.getUser();
        if(LX.isEmpty(user)) return OS.Page.toLogin();
        LX.exObj(user.get("menus"),"没有任何访问权限!");
        if ("admin".equals(user.get(OS.USERNAME)) || "#menus#".equals(user.get("menus"))){//如果是amin用户 或 所有权限
            ls =  redis.find("system:menu",HashMap.class);
        }else{
            ls = redis.findAll("system:menu",HashMap.class,user.getStr("menus").split(","));
        }
        LX.exObj(ls,"没有任何访问权限!");
        ls.sort((o1,o2)->{return o1.get("id").hashCode()-o2.get("id").hashCode();});
        return ls;
    }

    //说明:admin管理员菜单
    /**{ ylx } 2019/5/9 16:14 */
    @RequestMapping("/admin_menu")
    @ResponseBody
    public Object admin_menu(HttpServletRequest request) throws Exception {
        HashMap user = OS.getUser();
        if(LX.isEmpty(user)) return OS.Page.toLogin();
        if ("admin".equals(user.get(OS.USERNAME))){
            return LX.toList(HashMap.class,"[" +
                    "{name='接口管理',url='/sys/sys/service/list.html'}" +
                    ",{name='菜单管理',url='/sys/sys/menu/list.html'}" +
                    ",{name='权限管理',url='/sys/sys/role/list.html'}" +
                    ",{name='用户管理',url='/sys/sys/user/list.html'}" +
                    "]");
        }
        return LX.toList(HashMap.class,"[]");
    }



    @Authority(false)
    @RequestMapping("/login/code")
    public void generate(HttpServletRequest req,HttpServletResponse response) throws IOException {
        genCaptcha(req,response);
    }

    public static void genCaptcha(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        char[] codeSequence ={'0'};
        char[] codeSequence1 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
                'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
        // 定义图像buffer
        BufferedImage buffImg = new BufferedImage(90, 40, BufferedImage.TYPE_INT_RGB);
        Graphics gd = buffImg.getGraphics();
        // 创建一个随机数生成器类
        Random random = new Random();
        // 将图像填充为白色
        gd.setColor(Color.decode("#FFFFFF"));
        gd.fillRect(0, 0, 90, 40);
        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Dialog", 0, 22);
        // 设置字体。
        gd.setFont(font);
        // 画边框。
        gd.setColor(Color.decode("#E0AB48"));
        gd.drawRect(0, 0, 90 - 1, 40 - 1);

        // 随机产生40条干扰线，使图象中的认证码不易被其它程序探测到。
        gd.setColor(Color.decode("#000000"));
        for (int i = 0; i < 17; i++) {
            int x = random.nextInt(90);
            int y = random.nextInt(40);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }

        // randomCode用于保存随机产生的验证码，以便用户登录后进行验证。
        StringBuffer randomCode = new StringBuffer();
        int red = 0, green = 0, blue = 0;

        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < 4; i++) {
            // 得到随机产生的验证码数字。
            String code = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            // 用随机产生的颜色将验证码绘制到图像中。
            gd.setColor(new Color(red, green, blue));
            gd.drawString(code, (i + 1) * 15, 27);

            // 将产生的四个随机数组合在一起。
            randomCode.append(code);
        }
        // 将四位数字的验证码保存到Session中。
        HttpSession session = req.getSession();
        session.setAttribute("code", randomCode.toString());
        // 禁止图像缓存。
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);

        resp.setContentType("image/jpeg");
        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos = resp.getOutputStream();
        ImageIO.write(buffImg, "jpeg", sos);
        sos.close();
    }
}
