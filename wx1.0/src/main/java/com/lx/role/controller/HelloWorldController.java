package com.lx.role.controller;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.entity.TGRespose;
import com.lx.entity.WxMessage;
import com.lx.service.QueryService;
import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 创建人:游林夕/2019/4/26 19 13
 */
@RestController
public class HelloWorldController {
    @Autowired
    private QueryService queryService;
    @Autowired
    private RedisUtil redisUtil;

    Logger log = LoggerFactory.getLogger(HelloWorldController.class);
    @RequestMapping("/wx")
    public void wx(HttpServletRequest req, HttpServletResponse response) throws Exception {
        String msg = "";
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(),"utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line=br.readLine())!=null){
                sb.append(line);
            }
            System.out.println(sb);
            log.info(sb.toString());
            String str = sb.toString();
            String name = getVal(sb.toString(),"FromUserName").replace("<![CDATA[","").replace("]]>","");

            if ("<![CDATA[text]]>".equals(getVal(sb.toString(),"MsgType"))){
                WxMessage m = new WxMessage(getTrim(str,"Content"),name,name,name,"");
                if (m.getText().contains("https://m.tb.cn")){
                    TGRespose tg = queryService.tbgw(m);
                    msg = parseTW(str,tg);
                }else{
                    List<String> ls = queryService.wx_in(m);
                    if (ls!=null){
                        log.info("获取消息成功!"+ls.get(0));
                        msg = parse(str,ls.get(0));
                    }
                }

            }
        }catch (Exception e){
            log.error("微信错误",e);
        }finally {
            //根据消息自动回复
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter pw = response.getWriter();
            pw.println(msg);
            pw.close();
        }

    }
    //说明:回复图文消息
    /**{ ylx } 2019/7/3 23:07 */
    public String parseTW(String str, TGRespose tg) throws Exception {
        String uuid = LX.uuid32(5);
        redisUtil.put("app:gw:"+uuid,LX.toMap("{imgUrl='{0}',tkl='{1}'}"
                ,tg.getImgUrl(),tg.getUrl()),2*24*60*60);
        return "<xml>\n" +
                "  <ToUserName>"+getVal(str,"FromUserName")+"</ToUserName>\n" +
                "  <FromUserName>"+getVal(str,"ToUserName")+"</FromUserName>\n" +
                "  <CreateTime>"+System.currentTimeMillis()/1000L+"</CreateTime>\n" +
                "  <MsgType><![CDATA[news]]></MsgType>\n" +
                "  <ArticleCount>1</ArticleCount>\n" +
                "  <Articles>\n" +
                "    <item>\n" +
                "      <Title><![CDATA["+tg.getText()+"]]></Title>\n" +
                "      <Description><![CDATA["+tg.getTitle()+"]]></Description>\n" +
                "      <PicUrl><![CDATA["+tg.getImgUrl()+"]]></PicUrl>\n" +
                "      <Url><![CDATA[http://www.52ylx.cn/h/"+uuid+"]]></Url>\n" +
                "    </item>\n" +
                "  </Articles>\n" +
                "</xml>";
    }
    //说明:回复文本消息
    /**{ ylx } 2019/7/3 23:07 */
    public String parse(String str,String content){
        return "<xml>\n" +
                "  <ToUserName>"+getVal(str,"FromUserName")+"</ToUserName>\n" +
                "  <FromUserName>"+getVal(str,"ToUserName")+"</FromUserName>\n" +
                "  <CreateTime>"+System.currentTimeMillis()/1000L+"</CreateTime>\n" +
                "  <MsgType><![CDATA[text]]></MsgType>\n" +
                "  <Content><![CDATA["+content+"]]></Content>\n" +
                "</xml>";

    }
    public static String getTrim(String str,String key){
        return getVal(str,key).replace("<![CDATA[","").replace("]]>","");
    }
    public static String getVal(String str,String key) {
        return str.substring(str.indexOf(key)+key.length()+1,str.indexOf("</"+key));
    }

}
