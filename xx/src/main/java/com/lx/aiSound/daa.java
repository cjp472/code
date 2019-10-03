package com.lx.aiSound;//说明:



import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建人:游林夕/2019/5/8 22 32
 */
public class daa {
    public static void main(String[] args) throws Exception {
        String name = "绿色";
        String s = doPost("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+name+"&page=1&pagesize=20&showtype=1", null,60000);
        System.out.println(s);
    }
    static String doPost(String url, Map<String, String> param, int timeout) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            out = new PrintWriter(conn.getOutputStream());
            out.print(parse(param));
            out.flush();

            String line;
            for(in = new BufferedReader(new InputStreamReader(conn.getInputStream())); (line = in.readLine()) != null; result = result + line) {
                ;
            }
        } catch (Exception var17) {
            var17.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }
            } catch (IOException var16) {
                ;
            }

        }

        return result;
    }


    private static String parse(Map<String, String> map) {
        if (map == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            Iterator var2 = map.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, String> e = (Map.Entry)var2.next();
                sb.append("&").append((String)e.getKey()).append("=").append((String)e.getValue());
            }

            return sb.substring(1);
        }
    }
}