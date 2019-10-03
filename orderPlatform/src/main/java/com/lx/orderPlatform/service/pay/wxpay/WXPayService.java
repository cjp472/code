package com.lx.orderPlatform.service.pay.wxpay;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import com.lx.orderPlatform.service.pay.OnlinePay;
import com.lx.orderPlatform.util.Tools;
import com.lx.util.LX;
import com.lx.util.LogUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fh.util.PageData;

@Service("weixinService")
public class WXPayService  implements OnlinePay {

    private String notify_url="http://127.0.0.1";
    @Autowired
    private Tools tools;
    private Map<String,WXPay> map = new HashMap<>(32);

    /**
     * 生成二维码地址
     */
    @Override
    public PageData getQRcode(PageData pd) throws Exception {
        return getEWMPayInfo(pd);
    }
    @Override
    public PageData facePayment(PageData pd) throws Exception {
        LX.exMap(pd, "OrderNo,OrderMoney,auth_code,HID");
        BigDecimal b =LX.getBigDecimal(pd.get("OrderMoney")) ;
        BigDecimal money = b.multiply(new BigDecimal("100")).setScale(0);//精确到分
        WXPay wx = getWXPay(new PageData("HID",pd.get("HID")));
        PageData p = new PageData();
        p.put("attach", "付款");
        p.put("body", "订单号:"+pd.get("OrderNo"));
        p.put("spbill_create_ip", "111.164.176.145");
        p.put("total_fee", money.toString());
        p.put("auth_code", pd.get("auth_code"));
        p.put("out_trade_no", pd.get("OrderNo"));
        p.put("time_expire",getTimeout());
        PageData ps =LX.toMap(PageData.class,wx.microPay(p));
        if(ps == null  ) throw new Exception("调用当面付失败!");
        orderQuery(pd);//查询订单
        return new PageData("isSuccess","1");
    }

    public  PageData getEWMPayInfo(PageData pd) throws Exception {
        if(LX.isEmpty(pd.get("OrderNo"))) throw new Exception("wxpay没有获取到OrderNo");
        if(LX.isEmpty(pd.get("OrderMoney"))) throw new Exception("wxpay没有获取到OrderMoney");
        BigDecimal b = LX.getBigDecimal(pd.get("OrderMoney"));
        BigDecimal money = b.multiply(new BigDecimal("100")).setScale(0);//精确到分
        WXPay wx = getWXPay(new PageData("HID",pd.get("HID")));
        PageData p = new PageData();
        p.put("attach", "付款");
        p.put("body", "订单号:"+pd.get("OrderNo"));
        p.put("spbill_create_ip", "111.164.176.145");
        p.put("total_fee", money.toString());
        p.put("trade_type", "NATIVE");
        p.put("out_trade_no", pd.get("OrderNo"));
        p.put("time_expire",getTimeout());
        PageData ps =LX.toMap(PageData.class,wx.unifiedOrder(p));
        if(ps == null || LX.isEmpty(ps.get("code_url"))) throw new Exception("获取微信二维码地址失败!");
        return new PageData("qrcode",ps.get("code_url"));
    }

    //说明:设置2分钟超时
    /**创建人:游林夕 -- 2019/1/21 14:34 --*/
    private String getTimeout(){
        long time = new Date().getTime()+2*60*1000;
        Date d = new Date();
        d.setTime(time);
        return new SimpleDateFormat("yyyyMMddHHmmss").format(d);
    }

    @Override
    public  PageData getPayInfo(PageData pd) throws Exception {
        if(LX.isEmpty(pd.get("OrderNo"))) throw new Exception("wxpay没有获取到OrderNo");
        if(LX.isEmpty(pd.get("OrderMoney"))) throw new Exception("wxpay没有获取到OrderMoney");
        BigDecimal b =LX.getBigDecimal(pd.get("OrderMoney"));
        BigDecimal money = b.multiply(new BigDecimal("100")).setScale(0);//精确到分
        WXPay wx = getWXPay(new PageData("HID",pd.get("HID")));
        PageData p = new PageData();
        p.put("attach", "付款");
        p.put("body", "订单号:"+pd.get("OrderNo"));
        p.put("spbill_create_ip", "111.164.176.145");
        p.put("total_fee", money.toString());
        p.put("trade_type", "APP");
        p.put("out_trade_no", pd.get("OrderNo"));
        PageData ps =LX.toMap(PageData.class,wx.unifiedOrder(p));
        LX.exObj(ps);
        if(LX.isEmpty(ps.get("prepay_id"))) throw new Exception(ps.getString("err_code_des"));
        PageData pp = new PageData();
        pp.put("timestamp", WXPayUtil.getCurrentTimestamp()+"");
        pp.put("package", "Sign=WXPay");
        pp.put("prepayid", ps.get("prepay_id"));
        PageData d = LX.toMap(PageData.class,wx.report(pp));
        String s = LX.toJSONString(d);
        PageData out = new PageData("wxpay",s);
        return out;
    }



    @Override
    public PageData reconciliation(PageData pd) throws Exception {
        PageData out =new PageData();
        if(LX.isEmpty(pd.get("date")))throw new Exception("获取指定日期失败");
        out.put("bill_date", pd.getString("date").replace("-", ""));
        out.put("bill_type", "ALL");
        WXPay wx = getWXPay(new PageData("HID",pd.get("HID")));
        PageData d = LX.toMap(PageData.class,wx.downloadBill(out));
        PageData ls = new PageData();
        if(WXPayConstants.SUCCESS.equals(d.getString("return_code"))&&"ok".equals(d.getString("return_msg"))){
            String str = d.getString("data");
            String [] ar = str.split("\\n");
            List<PageData> list =new ArrayList();
            for (int i = 1; i < ar.length; i++) {
                if(ar[i].startsWith("`")){
                    ar[i] = ar[i].replace("`", "");
                    String [] arr = ar[i].split(",");
                    if(arr.length >= 24){
                        //交易时间,公众账号ID,商户号,子商户号,设备号,微信订单号,商户订单号,用户标识,交易类型,交易状态,付款银行,货币种类,总金额,企业红包金额,微信退款单号,商户退款单号,退款金额,企业红包退款金额,退款类型,退款状态,商品名称,商户数据包,手续费,费率
                        if(arr[5].matches("\\d+")){
                            PageData p = new PageData();
                            p.put("PayMoney", arr[12]);//订单金额（元）
                            if(new BigDecimal(p.getStr("PayMoney")).compareTo(new BigDecimal("0"))>0){
                                p.put("PayStatus","2");//支付状态 付款
                            }else{
                                p.put("PayMoney", new BigDecimal(arr[16]).multiply(new BigDecimal("-1")));//订单金额（元）
                                p.put("PayStatus","3");//支付状态  退款
                            }
                            p.put("OrderNo", arr[6].trim());//商户订单号
                            list.add(p);
                        }
                    }
                }
            }
            ls.put("detaile", list);
        }
        return ls;
    }
    /**
     * 	查询订单
     *  @author
     *  创建时间：2018年4月18日 下午1:32:36
     */
    public PageData orderQuery(PageData order) throws Exception{
        LX.exMap(order,"OrderNo,HID");
        PageData out =new PageData();
        out.put("out_trade_no", order.get("OrderNo"));
        WXPay wx = getWXPay(new PageData("HID",order.get("HID")));
        PageData d = LX.toMap(PageData.class,wx.orderQuery(out));
        //SUCCESS—支付成功 REFUND—转入退款 NOTPAY—未支付 CLOSED—已关闭 REVOKED—已撤销（刷卡支付） USERPAYING--用户支付中 PAYERROR--支付失败(其他原因，如银行返回失败)
        if(WXPayConstants.REFUND.equals(d.get("trade_state"))){
            return LX.toMap(PageData.class,"{OrderNo='{0}',OrderStatus='5',OrderMoney='{1}',retBusinessSN='{2}'}",d.get("out_trade_no"),LX.getBigDecimal(d.get("total_fee")).divide(LX.getBigDecimal(100)),d.get("transaction_id"));
        }else if(WXPayConstants.SUCCESS.equals(d.get("trade_state"))){
            return LX.toMap(PageData.class,"{OrderNo='{0}',OrderStatus='2',OrderMoney='{1}',BusinessSN='{2}'}",d.get("out_trade_no"),LX.getBigDecimal(d.get("total_fee")).divide(LX.getBigDecimal(100)),d.get("transaction_id"));
        }
        return d;
    }

    @Override
    public PageData tradeRefund(PageData order) throws Exception {
        LX.exMap(order,"HID,refundAmount,OrderNo");
        WXPay wx = getWXPay(new PageData("HID",order.get("HID")));
        PageData p = new PageData();
        BigDecimal b =LX.getBigDecimal(order.get("refundAmount"));
        BigDecimal money = b.multiply(new BigDecimal("100")).setScale(0);//精确到分
        p.put("total_fee", money.toString());
        p.put("refund_fee", money.toString());
        p.put("transaction_id", ""); // 订单号
        p.put("out_trade_no", order.get("OrderNo")); // 订单号
        p.put("out_refund_no", order.get("OrderNo"));//退款订单号
        PageData params =LX.toMap(PageData.class,wx.refund(p));//验完签
        return new PageData();
    }

    @Override
    public PageData findPayConfig(PageData pd) throws Exception {
        LX.exMap(pd,"HID");
        PageData map = LX.toMap(PageData.class
                ,"{WXAppid='{0}',WXMchID='{1}',WXKey='{2}'}"
                ,tools.getProperty(pd.getStr("HID")+".WXAppid")
                ,tools.getProperty(pd.getStr("HID")+".WXMchID")
                ,tools.getProperty(pd.getStr("HID")+".WXKey")
        );
        try{
            LX.exMap(map,"WXAppid,WXMchID,WXKey");
        }catch (Exception e){
            LX.exMsg("暂未开通微信支付!");
        }
        return map;
    }

    /**
     * 获取微信支付类
     *  @author
     *  创建时间：2018年4月17日 下午2:55:54
     */
    public WXPay getWXPay(PageData pd) throws Exception{
        if (!map.containsKey(pd.getStr("HID"))){//不包含该支付类
            synchronized (WXPayService.class){
                if (!map.containsKey(pd.getStr("HID"))){
                    WXPayConfigImpl con = new WXPayConfigImpl();
                    con.setMap(findPayConfig(new PageData("HID",pd.get("HID"))));
                    map.put(pd.getStr("HID"),new WXPay(con,notify_url));
                }
            }
        }
        return map.get(pd.getStr("HID"));

    }
}
