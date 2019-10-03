package com.lx.orderPlatform.controller;//说明:

import com.fh.util.PageData;
import com.lx.orderPlatform.service.pay.PayService;
import com.lx.util.LX;
import com.lx.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.*;

/**
 * 创建人:游林夕/2019/4/24 17 12
 * @author 游林夕
 */
@RestController
@RequestMapping(value="/app")
public class AppController{
    private Logger log = LoggerFactory.getLogger(AppController.class);
    @Autowired
    private PayService payService;

    @RequestMapping(value = "/service")
    @ResponseBody
    public Object service(HttpServletRequest req) throws Exception {
        PageData pd = null;
        try {
            pd = LX.toMap(PageData.class,getParameterMap(req));
//            getIpAddress(req);
            LX.exMap(pd, "data");
            pd = LX.toMap(PageData.class,LX.decodeV1(pd.getStr("data")));
            LX.exMap(pd, "method,HID,t,s");
            if (!LX.md5(pd.getStr("HID")+pd.get("method")+LX.getDate("yyyyMMddHHmmss",Long.parseLong(pd.getStr("t")))).equals(pd.getStr("s"))){
                LX.exMsg("验证失败!");
            }
            log.info("=====================================================");
            log.info("调用:method="+pd.get("method")+pd.toString());
            Method method = payService.getClass().getDeclaredMethod(pd.getStr("method"),PageData.class);
            Object obj =  method.invoke(payService, pd);
            log.info("返回:method="+pd.get("method")+obj.toString());
            return obj;
        }catch (Exception e){
            log.error("调用异常:",e);
            Object obj = LX.toMap("{'success'='0',msg='{0}'}",e.getCause()==null?e.getMessage():e.getCause().getMessage());
            if (LX.isNotEmpty(pd)){
                log.info("异常:method="+pd.get("method")+pd.toString()+"==>"+obj.toString());
            }
            return obj;
        }

    }
    /**获取请求参数*/
    private Map<String, String> getParameterMap(HttpServletRequest request) {
        // 参数Map
        Map<?, ?> properties = request.getParameterMap();
        // 返回值Map
        Map<String, String> returnMap = new HashMap<String, String>(32);
        Iterator<?> entries = properties.entrySet().iterator();

        Map.Entry<String, Object> entry;
        String name = "";
        String value = "";
        Object valueObj =null;
        while (entries.hasNext()) {
            entry = (Map.Entry<String, Object>) entries.next();
            name = (String) entry.getKey();
            valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }


    public static void main(String[]args) throws Exception {
        call("2019010332","getQRcode","3");
//        call("20190101","orderQuery","6");
//        call("20190202","facePayment","6");
//        call("1","reconciliation","3");
    }
    private static void call(String OrderNo,String method,String payType) throws Exception {
        PageData pd = LX.toMap(PageData.class
                ,"{'OrderMoney'='0.01','OrderNo'='{1}','HID'='C2D54645-9B39-46DB-95CA-9187BF246C42','method'='{2}',payType='{3}',t='{0}',date='{4}'}"
                ,System.currentTimeMillis(),OrderNo,method,payType,LX.getDate("yyyy-MM-dd",System.currentTimeMillis()-1000L*60*60*24));
        pd.put('s',LX.md5(pd.getStr("HID")+pd.get("method")+LX.getDate("yyyyMMddHHmmss",Long.parseLong(pd.getStr("t")))));
        pd = new PageData("data",LX.encodeV1(LX.toJSONString(pd)));
        System.out.println("http://220.194.101.89:8081/app/service?"+pd);
//        String s = LX.doPost("http://220.194.101.89:8081/app/service", pd);
//        System.out.println(LX.toFormatJson(s));
    }
}
