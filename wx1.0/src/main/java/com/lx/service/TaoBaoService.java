package com.lx.service;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.entity.TGRespose;
import com.lx.mapper.QueryMapper;
import com.lx.util.LX;
import com.pdd.pop.sdk.common.util.StringUtils;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.NTbkOrder;
import com.taobao.api.request.*;
import com.taobao.api.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 创建人:游林夕/2019/6/28 18 53
 */
@Service
public class TaoBaoService {
    @Autowired
    private RedisUtil redisUtil;
    private String serverUrl = "http://gw.api.taobao.com/router/rest";
    private TaobaoClient client = new DefaultTaobaoClient(serverUrl, "26309418", "5aaa2a578663815a5faab64858937f52");
    private Random r = new Random();
    @Autowired
    private QueryMapper queryMapper;
    @Autowired
    private MyWxBot bot;

    public static String relationId = "2202286751";
//    @PostConstruct
    public void getOrder() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String time =redisUtil.get("app:tb:time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    long t1 = sdf.parse(time).getTime();
                    long bc = 1000L*60;
                    while (true){
                        Thread.sleep(1000);
                        if (t1+bc*2>new Date().getTime()){//大于当前时间
                            Thread.sleep(bc);
                            continue;
                        }
                        time=sdf.format(new Date(t1+=bc));
                        redisUtil.put("app:tb:time",time);
                        try{
                            List<NTbkOrder> ls = querOrder(time,12L);//付款时间
                            if (LX.isNotEmpty(ls)){
                                for (NTbkOrder it : ls){
                                    try{
                                        if (it.getCreateTime().getTime()<=t1) continue;
                                        Map map = LX.toMap("{'商品ID'='{0}','推广位ID'='{1}','淘宝订单编号'='{2}','付款预估收入'='{3}'}"
                                                ,it.getNumIid(),it.getAdzoneId(),it.getTradeParentId(),it.getPubSharePreFee());
                                        if (12==it.getTkStatus()){//已付款
                                            map.put("订单状态","已付款");
                                        }else if(3==it.getTkStatus()){//已结算
                                            map.put("订单状态","已结算");
                                        }
                                        Map<String,String> res = queryMapper.updateOrderInfo(map);
                                        if(LX.isEmpty(res)) continue;
                                        bot.send(res.get("fromRemarkName"),res.get("text").replace("@n","\n"));
                                    }catch (Exception e){
                                        MyWxBot.log.error("tb订单",e);
                                    }
                                }
                            }
                            ls = querOrder(time,3L);//结算时间
                            if (LX.isNotEmpty(ls)){
                                for (NTbkOrder it : ls){
                                    try{
                                        if (it.getCreateTime().getTime()<=t1) continue;
                                        Map map = LX.toMap("{'商品ID'='{0}','推广位ID'='{1}','淘宝订单编号'='{2}','付款预估收入'='{3}'}"
                                                ,it.getNumIid(),it.getAdzoneId(),it.getTradeParentId(),it.getPubSharePreFee());
                                        if (12==it.getTkStatus()){//已付款
                                            map.put("订单状态","已付款");
                                        }else if(3==it.getTkStatus()){//已结算
                                            map.put("订单状态","已结算");
                                        }
                                        Map<String,String> res = queryMapper.updateOrderInfo(map);
                                        if(LX.isEmpty(res)) continue;
                                        bot.send(res.get("fromRemarkName"),res.get("text").replace("@n","\n"));
                                    }catch (Exception e){
                                        MyWxBot.log.error("tb订单",e);
                                    }
                                }
                            }
                        }catch (Exception e){
                            Thread.sleep(1000L*60*5);
                            MyWxBot.log.error("tb",e);
                        }
                    }
                }catch (Exception e){
                    MyWxBot.log.error("tb",e);
                }
            }
        }).start();

    }

    public List<NTbkOrder> querOrder(String time, long type) throws ApiException {
        TbkOrderGetRequest req = new TbkOrderGetRequest();
        req.setFields("tk_status,adzone_id,pub_share_pre_fee,num_iid,create_time,earning_time");
        req.setStartTime(StringUtils.parseDateTime(time));
        req.setSpan(60L);
        req.setPageNo(1L);
        req.setPageSize(100L);
        req.setTkStatus(type);
        req.setOrderQueryType(type==12?"create_time":"settle_time");
        TbkOrderGetResponse rsp = client.execute(req);
        return rsp.getResults();
    }
    @PostConstruct
    public void getNewOrder() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String time =redisUtil.get("app:tb:time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    long t1 = sdf.parse(time).getTime();
                    long bc = 1000L*60;
                    while (true){
                        Thread.sleep(1000);
                        if (t1+bc*2>new Date().getTime()){//大于当前时间
                            Thread.sleep(bc);
                            continue;
                        }
                        time=sdf.format(new Date(t1+=bc));
                        redisUtil.put("app:tb:time",time);
                        try{
                            List<TbkOrderDetailsGetResponse.PublisherOrderDto> ls = queryNew(time,sdf.format(new Date(t1+bc-1000L)),12L);
                            if (LX.isNotEmpty(ls)){
                                for (TbkOrderDetailsGetResponse.PublisherOrderDto it : ls){
                                    try{
                                        Map map = LX.toMap("{'商品ID'='{0}','推广位ID'='{1}','淘宝订单编号'='{2}','付款预估收入'='{3}'}"
                                                ,it.getItemId(),it.getAdzoneId(),it.getTradeParentId(),it.getPubSharePreFee());
                                        if (12==it.getTkStatus()){//已付款
                                            map.put("订单状态","已付款");
                                        }else if(3==it.getTkStatus()){//已结算
                                            map.put("订单状态","已结算");
                                        }
                                        Map<String,String> res = queryMapper.updateOrderInfo(map);
                                        if(LX.isEmpty(res)) continue;
                                        bot.send(res.get("fromRemarkName"),res.get("text").replace("@n","\n"));
                                    }catch (Exception e){
                                        MyWxBot.log.error("tb订单",e);
                                    }
                                }
                            }
                            ls = queryNew(time,sdf.format(new Date(t1+bc-1000L)),3L);
                            if (LX.isNotEmpty(ls)){
                                for (TbkOrderDetailsGetResponse.PublisherOrderDto it : ls){
                                    try{
                                        Map map = LX.toMap("{'商品ID'='{0}','推广位ID'='{1}','淘宝订单编号'='{2}','付款预估收入'='{3}'}"
                                                ,it.getItemId(),it.getAdzoneId(),it.getTradeParentId(),it.getPubSharePreFee());
                                        if (12==it.getTkStatus()){//已付款
                                            map.put("订单状态","已付款");
                                        }else if(3==it.getTkStatus()){//已结算
                                            map.put("订单状态","已结算");
                                        }
                                        Map<String,String> res = queryMapper.updateOrderInfo(map);
                                        if(LX.isEmpty(res)) continue;
                                        bot.send(res.get("fromRemarkName"),res.get("text").replace("@n","\n"));
                                    }catch (Exception e){
                                        MyWxBot.log.error("tb订单",e);
                                    }
                                }
                            }
                        }catch (Exception e){
                            Thread.sleep(1000L*60*5);
                            MyWxBot.log.error("tb",e);
                        }
                    }
                }catch (Exception e){
                    MyWxBot.log.error("tb",e);
                }
            }
        }).start();

    }

    //12-付款，13-关闭，14-确认收货，3-结算成功
    public List<TbkOrderDetailsGetResponse.PublisherOrderDto> queryNew(String time, String end, long type) throws ApiException {
        TbkOrderDetailsGetRequest req = new TbkOrderDetailsGetRequest();
        //查询时间类型，1：按照订单淘客创建时间查询，2:按照订单淘客付款时间查询，3:按照订单淘客结算时间查询
        req.setQueryType(type==12?2L:3L);
        req.setPageSize(100L);
        req.setTkStatus(type);
        req.setStartTime(time);
        req.setEndTime(end);
        req.setPageNo(1L);
        req.setOrderScene(1L);
        TbkOrderDetailsGetResponse rsp = client.execute(req);
        if (rsp == null || rsp.getData() == null) return null;
        return rsp.getData().getResults();
    }


    //settle_time 3：订单结算，create_time  12：订单付款

    Random ran = new Random();
    public String getTGUrl() throws Exception {
        TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
        req.setPageSize(30L);
        req.setPageNo(ran.nextInt(3)*1L);
        req.setPlatform(2L);
        req.setSort("total_sales_des");
        req.setQ("综合");
        req.setHasCoupon(true);//有优惠券
        req.setAdzoneId(108824400102L);
        req.setNeedFreeShipment(true);//包邮
        req.setIncludePayRate30(true);//成交
        req.setIncludeGoodRate(true);//好评
        TbkDgMaterialOptionalResponse rsp = client.execute(req);
        List<Map> ls = new ArrayList<>();
        for (TbkDgMaterialOptionalResponse.MapData nt : rsp.getResultList()) {
            String url = nt.getCouponShareUrl();
            if (LX.isEmpty(url)) url = nt.getUrl();
            if (url.startsWith("//")) {
                url = "https:" + url;
            }
            if (LX.isEmpty(nt.getCouponInfo())) {
                continue;
            }
            BigDecimal to = LX.getBigDecimal(nt.getZkFinalPrice());
            String s = nt.getCouponInfo().substring(nt.getCouponInfo().indexOf("减") + 1, nt.getCouponInfo().length() - 1);
            to = to.subtract(LX.getBigDecimal(s));
            Map map = LX.toMap("{img='{0}',title='{1}',tkl='{2}',yhq='{3}',zj='{4}',qh='{5}',url='{6}'}"
                ,nt.getSmallImages() == null ? nt.getPictUrl() : nt.getSmallImages().get(0)
                ,nt.getTitle(),toTKL(url, nt.getPictUrl(), nt.getTitle()),nt.getCouponInfo(),nt.getZkFinalPrice(),to,url);
            ls.add(map);
        }
        String uuid = LX.uuid32(5);
        redisUtil.put("app:tg:"+uuid,ls,2*24*60*60);
        return "http://52ylx.cn/tg/"+uuid;
    }

    /**{ ylx } 2019/5/19 15:44 */
    public TGRespose findFL(String in_title, long numiid, long adzoneId) throws ApiException {

        TbkItemInfoGetResponse.NTbkItem item= getProduct(numiid+"");
        in_title = item.getTitle();
        String city = item.getProvcity();
        city = city.indexOf(" ")!=-1?city.substring(city.lastIndexOf(" ")):city;
//        if (in_title.contains("聚划算")){
//            in_title =in_title.substring(in_title.indexOf(":")+1,in_title.indexOf("("));
//        }else{
//            in_title=in_title.substring(in_title.indexOf("【")+1,in_title.lastIndexOf("】"));
//        }
//        in_title = in_title.replace("-","").replace("\\d","");
        TbkDgMaterialOptionalRequest req = new TbkDgMaterialOptionalRequest();
        req.setQ(in_title);
        req.setItemloc(city);
        req.setAdzoneId(adzoneId);
        req.setPlatform(1L);
        req.setPageSize(500L);
        req.setPageNo(1L);
        long pageNo = 1L;
        long res = 0L;
        do {
            TbkDgMaterialOptionalResponse rsp = client.execute(req);
            if (pageNo>10) return null;//搜索不到
            res = rsp.getTotalResults() / 100L+1;//每页100条
            req.setPageNo(pageNo += 1);//循环查询
            List<TbkDgMaterialOptionalResponse.MapData> ls = rsp.getResultList();
            if (LX.isNotEmpty(ls)) {
                for (TbkDgMaterialOptionalResponse.MapData nt : ls) {
                    if (nt.getNumIid() == numiid) {
                        String url = nt.getCouponShareUrl();
                        if (LX.isEmpty(url)) url = nt.getUrl();
                        if (url.startsWith("//")) {
                            url = "https:" + url;
                        }
                        url=url+"&relationId="+relationId;//渠道
                        BigDecimal to = LX.getBigDecimal(nt.getZkFinalPrice());
                        if (LX.isNotEmpty(nt.getCouponInfo())) {
                            String s = nt.getCouponInfo().substring(nt.getCouponInfo().indexOf("减") + 1, nt.getCouponInfo().length() - 1);
                            to = to.subtract(LX.getBigDecimal(s));
                        }
                        TGRespose t = new TGRespose(nt.getSmallImages() == null ? nt.getPictUrl() : nt.getSmallImages().get(0)
                                , toTKL(url, nt.getPictUrl(), nt.getTitle())
                                , numiid + "", in_title, "", to, LX.getBigDecimal(nt.getCommissionRate()).divide(LX.getBigDecimal(10)));
                        String out = "总: " + nt.getZkFinalPrice() + " ,返: " + t.getFx();
                        //返利金额小于1毛
//                        if (LX.compareTo(t.getFx(), "0.5", MathUtil.Type.LT)) return null;
                        if (LX.isNotEmpty(nt.getCouponInfo())) {
                            out += "\n" + nt.getCouponInfo() + "" +
                                    " 券后: " + to;
                        }
                        t.setText(out);
                        t.setQurl(url);
                        return t;
                    }
                }
            }
        }while (res>=pageNo);
        return null;
    }
    //说明:优惠券链接转淘口令
    /**{ ylx } 2019/5/17 23:07 */
    public String toTKL(String url,String imgUrl,String title) throws ApiException {
        if (url.startsWith("//s")){
            url="https:"+url;
        }
        TbkTpwdCreateRequest req = new TbkTpwdCreateRequest();
        req.setText(title);
        req.setLogo(imgUrl);
        req.setUrl(url);
        TbkTpwdCreateResponse rsp = client.execute(req);
        if (LX.isEmpty(rsp))return "";
        return rsp.getData().getModel();
    }

    //说明:通过喵有券获取商品id https://open.21ds.cn/index/index/openapi/id/5.shtml?ptype=1
    /**{ ylx } 2019/6/6 18:26 */
    public static long getID(String title) throws Exception {
//        String tkl = title.substring(title.indexOf("￥"), title.lastIndexOf("￥") + 1);
//        String url = LX.doGet("https://api.open.21ds.cn/apiv1/tpwdtoid?apkey=4541e955-5d12-f3fe-3856-d73af33d8a2d&tpwd="+ URLEncoder.encode(tkl, "utf-8"));
//        System.out.println(url);
        String url = LX.doGet("https://api.open.21ds.cn/apiv1/getgyurlbyall?apkey=4541e955-5d12-f3fe-3856-d73af33d8a2d&tbname=%E6%99%A8%E6%9B%A6%E8%90%B1%E6%99%B4&pid=mm_125968829_489050113_108824400102&content="+URLEncoder.encode(title, "utf-8"));
        return Long.parseLong(((Map)((Map)LX.toMap(url).get("result")).get("data")).get("item_id").toString());


    }

    public TbkItemInfoGetResponse.NTbkItem getProduct(String numid) throws ApiException {
        TbkItemInfoGetRequest req = new TbkItemInfoGetRequest();
        req.setNumIids(numid);
        TbkItemInfoGetResponse rsp = client.execute(req);
        LX.exObj(rsp,"没有获取到商品信息!");
        return rsp.getResults().get(0);
    }
}
