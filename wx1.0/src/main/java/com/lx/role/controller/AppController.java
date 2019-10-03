package com.lx.role.controller;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.service.ExcelUtilService;
import com.lx.service.TaoBaoService;
import com.lx.util.LX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * 创建人:游林夕/2019/5/22 14 07
 */
@Controller
public class AppController {

    @Autowired
    private ExcelUtilService excelUtilService;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TaoBaoService taoBaoService;

    protected String dir = System.getProperty("user.dir");//地址

    @RequestMapping(value = "/zfb")
    public void alipay_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String message = "success";
        PrintWriter out = response.getWriter();
        out.println(message);
        out.flush();
        out.close();
    }

    @RequestMapping("/h/{main}")
    public String toMain(@PathVariable("main") String main,HttpServletRequest req,HttpServletResponse res) throws UnsupportedEncodingException {
//        if (isAndroid(req)&&isWechat(req)){//安卓手机用微信浏览器打开
//            String fileName = "upload.apk";
//            res.setHeader("content-type", "text/plain; charset=utf-8");
//            res.setContentType("text/plain; charset=utf-8");
//            res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
//            res.setHeader("Content-Length","0");
//            res.setHeader("Content-Range","bytes 0-1/1");
//            res.setHeader("Accept-Ranges", "bytes");
//            res.setHeader("Connection","keep-alive");
//            res.setHeader("Content-Range","bytes 0-1/1");
//            res.setHeader("ETag","W/\"0-2jmj7l5rSw0yVb/vlWAYkK/YBwk\"");
//            res.setHeader("X-Powered-By","Express");
//            return null;
//        }else{
            String str = redisUtil.get("app:gw:"+main);
            return "redirect:/main.html?data="+URLEncoder.encode(str,"utf-8");
//        }
    }
    //说明:推广优惠券信息
    /**{ ylx } 2019/6/30 1:05 */
    @RequestMapping("/tg/{main}")
    public String toTG(@PathVariable("main") String main,HttpServletRequest req,HttpServletResponse res) throws UnsupportedEncodingException {
        if (isAndroid(req)&&isWechat(req)){//安卓手机用微信浏览器打开
            String fileName = "upload.apk";
            res.setHeader("content-type", "text/plain; charset=utf-8");
            res.setContentType("text/plain; charset=utf-8");
            res.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            res.setHeader("Content-Length","0");
            res.setHeader("Content-Range","bytes 0-1/1");
            res.setHeader("Accept-Ranges", "bytes");
            res.setHeader("Connection","keep-alive");
            res.setHeader("Content-Range","bytes 0-1/1");
            res.setHeader("ETag","W/\"0-2jmj7l5rSw0yVb/vlWAYkK/YBwk\"");
            res.setHeader("X-Powered-By","Express");
            return null;
        }else{
            return "redirect:/tg.html?id="+main;
        }
    }
    //说明:获取所有的推广信息
    /**{ ylx } 2019/6/29 23:40 */
    @RequestMapping("/tgs")
    @ResponseBody
    public String tgs(String id){
       return redisUtil.get("app:tg:"+id);
    }


    @RequestMapping("/excel")
    public String uploadExcel(String path){
        return "upload.html";
    }

    @RequestMapping("/fileUpload")
    @ResponseBody
    public Map fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        excelUtilService.save(file.getInputStream());
        return LX.toMap("{msg=success}");
    }
    //说明:推送消息
    /**{ ylx } 2019/6/29 20:51 */
    @RequestMapping("/ts")
    @ResponseBody
    public Map ts() throws Exception {
        excelUtilService.ts();
        return LX.toMap("{msg=success}");
    }


    @RequestMapping("/re")
    @ResponseBody
    public String updateRelationId(String re){
        taoBaoService.relationId = re;
        return taoBaoService.relationId;
    }

    /**
     * 判断 移动端/PC端
     * @Title: isMobile
     * @author: pk
     * @Description: TODO
     * @param request
     * @return
     * @return: boolean
     */
    public static boolean isMobile(HttpServletRequest request) {
        List<String> mobileAgents = Arrays.asList("ipad", "iphone os", "rv:1.2.3.4", "ucweb", "android", "windows ce", "windows mobile");
        String ua = request.getHeader("User-Agent").toLowerCase();
        for (String sua : mobileAgents) {
            if (ua.indexOf(sua) > -1) {
                return true;//手机端
            }
        }
        return false;//PC端
    }

    public static boolean isAndroid(HttpServletRequest request){
        return request.getHeader("User-Agent").toLowerCase().indexOf("android") > -1;//PC端
    }

    /**
     * 是否微信浏览器
     * @Title: isWechat
     * @author: pk
     * @Description: TODO
     * @param request
     * @return
     * @return: boolean
     */
    public static boolean isWechat(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent").toLowerCase();
        if (ua.indexOf("micromessenger") > -1) {
            return true;//微信
        }
        return false;//非微信手机浏览器

    }
}
