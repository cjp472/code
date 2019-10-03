package com.lx.orderPlatform.service.pay.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.fh.util.*;
import com.lx.orderPlatform.service.pay.OnlinePay;
import com.lx.orderPlatform.util.Tools;
import com.lx.util.LX;
import com.lx.util.LogUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * App支付
 * @author 游林夕
 *
 */
@Service("alipayService")
public class AlipayService implements OnlinePay{
    private String notify_url ="http://127.0.0.1";
    @Autowired
    private Tools tools;
    private Map<String,AlipayClient> map = new HashMap<>(32);

    /**当面付*/
    public PageData facePayment(PageData order) throws Exception {
        LX.exMap(order,"OrderMoney,OrderNo,auth_code");
        BigDecimal b1 = LX.getBigDecimal(order.get("OrderMoney"));
        BigDecimal money = b1.setScale(2);
        AlipayClient alipayClient = getClient(order);
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+order.get("OrderNo")+"\"," +
                "\"scene\":\"bar_code\"," +
                "\"auth_code\":\""+order.get("auth_code")+"\"," +
                "\"subject\":\""+"付款" +"\"," +
                "\"body\":\"订单号:"+order.getString("OrderNo") +"\"," +
                "\"timeout_express\":\"2m\"," +
                "\"total_amount\":"+money.toString() +
                "  }");
        request.setNotifyUrl(notify_url);
        AlipayTradePayResponse response = alipayClient.execute(request);
        if(!response.isSuccess()){
            throw new Exception("调用当面付失败!  "+response.getSubMsg());
        }
        return new PageData("isSuccess","1");
    }
    /**
     * 查询退款订单
     * @param order
     * @return
     * @throws Exception
     */
    public PageData refundQuery(PageData order) throws Exception {
        if(LX.isEmpty(order.get("OrderNo"))) throw new Exception("请传入OrderNo");
        AlipayClient alipayClient = getClient(order);
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+order.get("OrderNo")+"\"," +
                "\"out_request_no\":\""+order.get("OrderNo")+"\"" +
                "  }");
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());
        if(response.isSuccess()){
            String OrderNo = response.getOutTradeNo();
            String money = response.getRefundAmount();
            if(order.get("OrderNo").equals(OrderNo)&&LX.isNotEmpty(money)) {
                PageData params = (PageData) LX.toMap(PageData.class, response.getBody()).get("alipay_trade_fastpay_refund_query_response");
                PageData pd = new PageData();
                return params;
            }
        }
        return null;
    }

    /**
     * 查询订单
     * @param order
     * @return
     * @throws Exception
     */
    public PageData findOrder(PageData order) throws Exception {
        AlipayClient alipayClient = getClient(order);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+order.getStr("OrderNo")+"\"" +
                "  }");
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if(response.isSuccess()){
            return (PageData) LX.toMap(PageData.class, response.getBody()).get("alipay_trade_query_response");

        } else {
            throw new Exception("调用支付宝查询订单失败:"+response.getSubMsg());
        }
    }
    /**
     *  当面付2.0生成支付二维码
     *  @author
     *  创建时间：2018年5月2日 下午3:14:22
     */
    public PageData getQRcode(PageData order) throws Exception {
        LX.exMap(order,"OrderMoney,OrderNo,HID");
        order.put("notify_url", notify_url);
        BigDecimal b =LX.getBigDecimal(order.get("OrderMoney"));
        BigDecimal money = b.setScale(2);
        AlipayClient alipayClient = getClient(order);
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\""+order.get("OrderNo")+"\"," +
                "\"total_amount\":"+money.toString() +"," +
                "\"subject\":\""+"付款" +"\"," +
                "\"timeout_express\":\"2m\"," +
                "\"body\":\"订单号:"+order.getString("OrderNo") +"\"" +
                "}");
        request.setNotifyUrl(notify_url);
        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        if(response.isSuccess()){
            return new PageData("qrcode",response.getQrCode());
        } else {
            throw new Exception("调用支付宝生成二维码失败");
        }
    }


    /**
     * 支付宝获取支付码
     */
    @Override
    public PageData getPayInfo(PageData order) throws Exception {
        order.put("notify_url", notify_url);
        LX.exMap(order,"OrderNo,OrderMoney,notify_url,Time");
        return createAlipayOrderString(order);
    }
    /**
     * 获取支付宝支付的配置信息
     */
    @Override
    public PageData findPayConfig(PageData pd) throws Exception {
        LX.exMap(pd,"HID");
        PageData map = LX.toMap(PageData.class
                ,"{Appid='{0}',PrivateKey='{1}',AlipayPublicKey='{2}'}"
                ,tools.getProperty(pd.getStr("HID")+".Appid")
                ,tools.getProperty(pd.getStr("HID")+".PrivateKey")
                ,tools.getProperty(pd.getStr("HID")+".AlipayPublicKey")
        );
        try{
            LX.exMap(map,"Appid,PrivateKey,AlipayPublicKey");
        }catch (Exception e){
            LX.exMsg("暂未开通支付宝支付!");
        }

        return map;
    }
    //说明:获取支付对象
    /**{ ylx } 2019/8/9 8:42 */
    private AlipayClient getClient(PageData pd) throws Exception {
        if (!map.containsKey(pd.getStr("HID"))) {//不包含该支付类
            synchronized (AlipayService.class) {
                if (!map.containsKey(pd.getStr("HID"))) {
                    PageData Configs = findPayConfig(pd);
                    AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", Configs.getString("Appid"),Configs.getString("PrivateKey"),"json","utf-8",Configs.getString("AlipayPublicKey"), "RSA2");//获得初始化的AlipayClient
                    map.put(pd.getStr("HID"),alipayClient);
                }
            }
        }
        return map.get(pd.getStr("HID"));
    }
    public PageData tradeRefund(PageData pd) throws Exception {
        LX.exMap(pd,"OrderNo,refundAmount");
        AlipayClient alipayClient = getClient(pd);
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + pd.getStr("OrderNo")+"\"," +
                "\"trade_no\":\"\"," +
                "\"refund_amount\":" + pd.getStr("refundAmount")+"," +
                "\"refund_reason\":\"" + pd.getStr("refundReason")+"\"" +
                "  }");
        request.setNotifyUrl(notify_url);
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        return new PageData();
    }

    /*
     * 	查询订单
     *  @author
     *  创建时间：2018年4月18日 下午1:32:36
     */
    public PageData orderQuery(PageData order) throws Exception{
        PageData pd = new PageData();
        PageData params = refundQuery(order);
        if (LX.isEmpty(params)){//没有退款
            params = findOrder(order);
            String status = params.getString("trade_status");
            if (!status.equals("TRADE_SUCCESS") && !status.equals("TRADE_FINISHED")) {
                throw new Exception("查询交易信息错误"+status);
            }
            return LX.toMap(PageData.class,"{OrderNo='{0}',OrderStatus='2',OrderMoney='{1}',BusinessSN='{2}'}",params.get("out_trade_no"),params.get("total_amount"),params.get("trade_no"));
        }else{
            return LX.toMap(PageData.class,"{OrderNo='{0}',OrderStatus='5',OrderMoney='{1}',retBusinessSN='{2}'}",params.get("out_trade_no"),params.get("refund_amount"),params.get("trade_no"));
        }
    }



    /**
     * 获取支付宝对账信息下载账单
     *  @author 游林夕
     *  创建时间：2018年2月1日 下午1:12:47
     */
    public PageData reconciliation(PageData pd) throws Exception{
        PageData out =new PageData();
        if(LX.isEmpty(pd.get("date")))throw new Exception("获取指定日期失败");
        AlipayClient alipayClient = getClient(pd);
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();//创建API对应的request类
        request.setBizContent("{" +
                "    \"bill_type\":\"trade\"," +
                "    \"bill_date\":\""+pd.getString("date")+"\"}"); //设置业务参数
        AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
        PageData entity = LX.toMap(PageData.class,LX.toMap(response.getBody()).get("alipay_data_dataservice_bill_downloadurl_query_response"));
        if(entity==null||LX.isEmpty(entity.get("bill_download_url"))){
            return null;
        }
        String urlStr = entity.get("bill_download_url").toString();
        HttpURLConnection httpUrlConnection = null;
        InputStream fis = null;
        try {
            URL url = new URL(urlStr);
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setConnectTimeout(5 * 1000);
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setRequestMethod("GET");
            httpUrlConnection.setRequestProperty("Charsert", "UTF-8");
            httpUrlConnection.connect();
            fis = httpUrlConnection.getInputStream();
            //解析文件
            Map<String,List<String>> map = readZipCvsFile(fis);
            List<PageData> list =new ArrayList();
            for(String key:map.keySet()){
                for(String line: map.get(key)){
                    String [] arr = line.split(",");
                    if(arr.length==25){
                        if(arr[0].trim().matches("\\d+")){//支付宝交易号
                            PageData p = new PageData();
//			                  		支付宝交易号,商户订单号,业务类型,商品名称,创建时间,完成时间,门店编号,门店名称,操作员,终端号,对方账户,订单金额（元）,商家实收（元）,支付宝红包（元）,集分宝（元）,支付宝优惠（元）,商家优惠（元）,券核销金额（元）,券名称,商家红包消费金额（元）,卡消费金额（元）,退款批次号/请求号,服务费（元）,分润（元）,备注
                            p.put("PayMoney", arr[11].trim());//订单金额（元）
                            if(new BigDecimal(p.getStr("PayMoney")).compareTo(new BigDecimal("0"))>0){
                                p.put("PayStatus","2");//支付状态 付款
                            }else{
                                p.put("PayStatus","3");//支付状态  退款
                            }
                            p.put("OrderNo", arr[1].trim());//商户订单号
                            list.add(p);
                        }
                    }
                }
            }
            out.put("detaile", list);
            return out;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fis!=null) fis.close();
//			        if(fos!=null) fos.close();
                if(httpUrlConnection!=null) httpUrlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out;
    }






    /**
     * 根据订单信息生成支付宝信息
     * * 创建时间：2018年1月2日 下午12:02:39
     * @author 游林夕
     */
    public PageData createAlipayOrderString(PageData pd) throws Exception{
        pd.put("subject", "付款");
        if(LX.isEmpty(pd.get("OrderNo"))) throw new Exception("Alipay没有获取到OrderNo");
        if(LX.isEmpty(pd.get("OrderMoney"))) throw new Exception("Alipay没有获取到OrderMoney");
        if(LX.isEmpty(pd.get("notify_url"))) throw new Exception("Alipay没有获取到notify_url");
        PageData Configs = findPayConfig(pd);
        Map<String, String> keyValues = new HashMap<String, String>();
        keyValues.put("app_id",Configs.getString("Appid"));
        BigDecimal b =(BigDecimal) pd.get("OrderMoney");
        BigDecimal money = b.setScale(2);
        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\""+money.toString() +"\",\"subject\":\""+pd.getString("subject") +"\",\"body\":\""+pd.getString("body") +"\",\"goods_type\":\"1\",\"out_trade_no\":\"" +pd.getString("OrderNo") +  "\"}");
        keyValues.put("charset", "utf-8");
        keyValues.put("method", "alipay.trade.app.pay");
        keyValues.put("sign_type", "RSA2");
        keyValues.put("timestamp",pd.get("Time").toString());
        keyValues.put("version", "1.0");
        keyValues.put("notify_url", pd.getString("notify_url"));
        String orderParam = buildOrderParam(keyValues);
        String privateKey =Configs.getString("PrivateKey") ;
        String sign = getSign(keyValues, privateKey,true );
        final String orderInfo = orderParam + "&" + sign;
        PageData pdOut=new PageData();
        pdOut.put("alipay_string", orderInfo);
        return pdOut;
    }
    /**
     * 构造支付订单参数信息
     *
     * @param map
     * 支付订单参数
     * @return
     */
    public static String buildOrderParam(Map<String, String> map) {
        List<String> keys = new ArrayList<String>(map.keySet());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            sb.append(buildKeyValue(key, value, true));
            sb.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        sb.append(buildKeyValue(tailKey, tailValue, true));

        return sb.toString();
    }
    /**
     * 拼接键值对
     *
     * @param key
     * @param value
     * @param isEncode
     * @return
     */
    private static String buildKeyValue(String key, String value, boolean isEncode) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(value);
            }
        } else {
            sb.append(value);
        }
        return sb.toString();
    }
    /**
     * 对支付参数信息进行签名
     *
     * @param map
     *            待签名授权信息
     *
     * @return
     */
    public static String getSign(Map<String, String> map, String rsaKey, boolean rsa2) {
        List<String> keys = new ArrayList<String>(map.keySet());
        // key排序
        Collections.sort(keys);

        StringBuilder authInfo = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            authInfo.append(buildKeyValue(key, value, false));
            authInfo.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        authInfo.append(buildKeyValue(tailKey, tailValue, false));

        String oriSign = SignUtils.sign(authInfo.toString(), rsaKey, rsa2);
        String encodedSign = "";

        try {
            encodedSign = URLEncoder.encode(oriSign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "sign=" + encodedSign;
    }







    public synchronized	static   Map<String,List<String>> readZipCvsFile(InputStream is) {
        ZipInputStream in=null;
        BufferedReader br = null;
        Map<String,List<String>> map =new HashMap<String,List<String>>();
        try{
            in = new ZipInputStream(is, Charset.forName("GBK"));
            //不解压直接读取,加上gbk解决乱码问题
            br = new BufferedReader(new InputStreamReader(in,"gbk"));
            ZipEntry zipFile;
            //循环读取zip中的cvs文件，无法使用jdk自带，因为文件名中有中文
            while ((zipFile=in.getNextEntry())!=null) {
                if (zipFile.isDirectory()){
                    //如果是目录，不处理
                }
                //获得cvs名字
                String fileName = zipFile.getName();
                System.out.println("-----"+fileName);
                List<String> list = new ArrayList<String>();
                //检测文件是否存在
                if (fileName != null && fileName.indexOf(".") != -1) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        list.add(line);
                    }
                }
                map.put(fileName, list);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            //关闭流
            try {
                if(br!=null){
                    br.close();
                }
                if(in!=null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }




}
