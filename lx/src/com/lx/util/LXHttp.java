package com.lx.util;//说明:


import com.lx.util.LX;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * 创建人:游林夕/2019/3/27 14 45
 */
class LXHttp {
    static String doGet(String url) {
        String result ="";
        try {
            String line;
            BufferedReader in = null;
            for(in = new BufferedReader(new InputStreamReader(doGetStream(url), "utf-8")); (line = in.readLine()) != null; result = result + line) {
                ;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return result;
    }
    public static InputStream doGetStream(String url) {
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            System.setProperty("sun.net.client.defaultConnectTimeout", "30000");
            System.setProperty("sun.net.client.defaultReadTimeout", "30000");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            return conn.getInputStream();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    static String doPost(String url,Map<String,Object>parm){
        return doPost(url,parm,30000);
    }
    static String doPost(String urlStr, String json) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");// 设置Content-Type
            connection.setDoOutput(true);// 设置是否向httpUrlConnection输出，post请求设置为true，默认是false
            System.setProperty("sun.net.client.defaultConnectTimeout", "300000");
            System.setProperty("sun.net.client.defaultReadTimeout", "300000");
            connection.setConnectTimeout(300000);
            connection.setReadTimeout(300000);
            try(PrintWriter printWriter = new PrintWriter(connection.getOutputStream())){
                printWriter.write(json);// 设置RequestBody
                printWriter.flush();
            }
            // 返回结果-字节输入流转换成字符输入流，控制台输出字符
            try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        }catch (Exception e){
            return LX.exMsg(e);
        }
    }
    static String doPost(String url, Map<String,Object> param,int timeout) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            System.setProperty("sun.net.client.defaultConnectTimeout", ""+timeout);
            System.setProperty("sun.net.client.defaultReadTimeout", ""+timeout);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));// 获取URLConnection对象对应的输出流
            out.print(parse(param));// 发送请求参数
            out.flush();// flush输出流的缓冲
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));// 定义BufferedReader输入流来读取URL的响应
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally{//使用finally块来关闭输出流、输入流
            try{
                if(out!=null)out.close();
                if(in!=null)in.close();
            }catch(IOException ex){}
        }
        return result;
    }
    //将Map转为请求串
    private static String parse(Map<String, Object> map){
        if(LX.isEmpty(map))return "";
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String,Object> e : map.entrySet()){
            sb.append("&").append(e.getKey()).append("=").append(e.getValue());
        }
        return sb.substring(1);
    }

}
