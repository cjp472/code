package com.lx.aiSound;//说明:

import com.lx.util.LX;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * 创建人:游林夕/2019/5/8 22 14
 */
public class WxTest {
    public static void main(String [] args) throws IOException, InterruptedException {
        String str = LX.doPost("https://login.weixin.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=1377482012272",null);
        System.out.println(str);
        String uuid = str.substring(str.indexOf("\"")+1,str.lastIndexOf("\""));
        InputStream is = doPost("https://login.weixin.qq.com/qrcode/"+uuid+"?t=webwx",null,16000);
        BufferedImage image = ImageIO.read(is);
        Image big = image.getScaledInstance(256, 256,Image.SCALE_DEFAULT);
        BufferedImage inputbig = new BufferedImage(256, 256,BufferedImage.TYPE_INT_BGR);
        inputbig.getGraphics().drawImage(image, 0, 0, 256, 256, null); //画图
        ImageIO.write(inputbig, "jpg", new File("D:\\tts9\\workspace\\xx\\src\\main\\java\\com\\lx\\aiSound/temp.jpg")); //将其保存在
        is.close();
        while (true){
            URL url = new URL("https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login?uuid="+uuid+"&tip=1&_=1377482045264");

            System.out.println("扫码成功:"+str);
            if (str.indexOf("\"")!=-1){
                String url1 = str.substring(str.indexOf("\"")+1,str.lastIndexOf("\""));
                System.out.println(url1);
                InputStream ins = doPost("https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact?r=1377482079876",null,10000);
                BufferedReader in;
                str = "";
                String line;
                for(in = new BufferedReader(new InputStreamReader(ins)); (line = in.readLine()) != null; str = str + line) {
                    ;
                }
                System.out.println(str);
                String xx = "https://webpush.wx.qq.com/cgi-bin/mmwebwx-bin/synccheck?r=1557329828429&skey=%40crypt_63e330f8_ed983d397be7aebdacf1e0575e32367e&sid=9f9OkaYyOI6FIAhc&uin=1267764281&deviceid=e521760749604908&synckey=1_675951783%7C2_675951803%7C3_675951719%7C11_675951289%7C201_1557329803%7C1000_1557303558%7C1001_1557303562&_=1557329796246";

                ins = doPost(xx,null,10000);
                str = "";
                for(in = new BufferedReader(new InputStreamReader(ins)); (line = in.readLine()) != null; str = str + line) {
                    ;
                }
                System.out.println(str);

                break;
            }
            Thread.sleep(1000);
        }
    }



    static InputStream doPost(String url, Map<String, String> param, int timeout) {
        PrintWriter out = null;
        InputStream in = null;
        String result = "";

        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            conn.setRequestProperty("Cookie","mm_lang=zh_CN; wxuin=1267764281; webwxuvid=cdf48c39929efbcdf663dc3855168cc976321fbdb7799c804bde3eeca0e63c82d516ac7b8c1c3f981f3a451377a1f2d3; wxpluginkey=1557303558; pgv_pvi=6852763648; pgv_si=s1188080640; _qpsvr_localtk=0.2543438604400119; RK=YTBtYMJIat; ptcz=8bf3075174f129b62534b4d5477b3a6f3539d8e2f39e0496fe7de17aab041e5d; wxsid=9f9OkaYyOI6FIAhc; webwx_data_ticket=gSe5d6YzAhkQvMwT8R2uHVvp; webwx_auth_ticket=CIsBEKOav4UFGoAB2JZx3snauLtwbJwn5H4esPpf85mrUgVger4SArqKy2FbLgojIzbcpb2YEq38LTHR0tetCIQipKrsMse8gLm54UuzyDAbnYfcUL+OCTm/Bh7bMeFAZONto/r6YGrk0USWbNQWVR6/jSdeG9sCkMDlR2VBTE2G5SPAUES9J/0hDc4=; wxloadtime=1557329802_expired");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            out = new PrintWriter(conn.getOutputStream());
            out.print(parse(param));
            out.flush();
            return conn.getInputStream();
        } catch (Exception var17) {
            var17.printStackTrace();
            LX.exMsg(var17.getMessage());
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

        return null;
    }

    private static String parse(Map<String, String> map) {
        if (LX.isEmpty(map)) {
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
