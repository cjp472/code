package com.lx.executor;//说明:


import com.lx.util.LX;

import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * 创建人:游林夕/2019/3/27 13 47
 */
public class Test {
    public static void main(String [] args) throws Exception {
        LX.before("阿斯达斯");
        LX.info(LX.doPost("https://gw.open.icbc.com.cn/api/qrcode/V2/generate?app_id=Oikeclo001&msg_id=urcnl24ciutr9&format=json&charset=utf-8&sign_type=RSA2&sign=TRFEWHYUFCEW&timestamp=2016-10-29 20:44:38&biz_content=\n"
                ,new HashMap<>()));
        try{
            LX.exMsg("大大");
        }catch (Exception e){
            e.printStackTrace();
            LX.error(e);
        }
        LX.after();
    }
    public static void test(){
        System.out.println("1");
    }

}
