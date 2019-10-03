package com.lx.aiSound;

import com.lx.aiSound.sounds.playSounds;
import com.lx.util.LX;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2019/2/16.
 */
public class Test {
    public static void main(String[] args) throws Exception {
//       getinfo("猪叫的声音") ;
        String mp3 ="http://fs.w.kugou.com/201905131225/82e1e542398d109954f7b1b0347a15d0/G126/M00/05/09/HocBAFxLAoeAT3BzAD1nWyW7V5M814.mp3";
        URL m = new URL(mp3);
        URLConnection c = m.openConnection();
        File file = new File(mp3);
        DataInputStream in1 = new DataInputStream(c.getInputStream());
        File f = new File("D:/你的.mp3");
        DataOutputStream out1 = new DataOutputStream(new FileOutputStream(f));
        byte[] buffer = new byte[2048];
        int count = 0;
        while ((count = in1.read(buffer)) > 0) {
            out1.write(buffer, 0, count);
        }
        out1.close();
        in1.close();
    }
    private static String getinfo(String info) throws Exception{
//        URL url = new URL("http://biz.turingos.cn/apirobot/dialog/homepage/chat");
//        // 打开和URL之间的连接
//        URLConnection conn = url.openConnection();
//        //设置超时时间
//        conn.setConnectTimeout(5000);
//        conn.setReadTimeout(15000);
//        conn.setRequestProperty("accept", "application/json, text/plain, */*");
//        conn.setRequestProperty("connection", "Keep-Alive");
//        conn.setRequestProperty("user-agent",
//                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//        // 发送POST请求必须设置如下两行
//        conn.setDoOutput(true);
//        conn.setDoInput(true);
//        // 获取URLConnection对象对应的输出流
//        PrintWriter out = new PrintWriter(conn.getOutputStream());
//        // 发送请求参数
//        out.print("deviceId=43e943e9-43e9-43e9-43e9-43e943e943e9&question="+info);
//        // flush输出流的缓冲
//        out.flush();
//        // 定义BufferedReader输入流来读取URL的响应
//        BufferedReader in = new BufferedReader( new InputStreamReader(conn.getInputStream(), "utf8"));
//        String result = "";
//        String line;
//        while ((line = in.readLine()) != null) {
//            result += line;
//        }
//        if(out!=null) out.close();
//        if(in!=null) in.close();
        String result =LX.doPost("http://biz.turingos.cn/apirobot/dialog/homepage/chat?deviceId=43e943e9-43e9-43e9-43e9-43e943e943e9&question="+info,null);
//        System.out.println(result);
        //解析对象：第一个参数：待解析的字符串 第二个参数结果数据类型的Class对象
        System.out.println(result);
        HashMap grade= LX.toMap(HashMap.class,result);
        List results = ((List)((Map)grade.get("data")).get("results"));
        System.out.println((String)((Map)((Map)results.get(0)).get("values")).get("text"));
        if (results.size()>1&&((Map)((Map)results.get(1)).get("values")).containsKey("voice")){
            String mp3 = (String)((Map)((Map)results.get(1)).get("values")).get("voice");
//            System.out.println(mp3);
            URL m = new URL(mp3);
            URLConnection c = m.openConnection();
            File file = new File(mp3);
            DataInputStream in1 = new DataInputStream(c.getInputStream());
            File f = new File("D:/aa.mp3");
             DataOutputStream out1 = new DataOutputStream(new FileOutputStream(f));
             byte[] buffer = new byte[2048];
             int count = 0;
             while ((count = in1.read(buffer)) > 0) {
                     out1.write(buffer, 0, count);
                 }
//             out.close();
//             in.close();
            new playSounds(new FileInputStream(f));
        }

        return (String)((Map)((Map)results.get(0)).get("values")).get("text");
    }
}
