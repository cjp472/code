package com.lx.service;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.entity.TGRespose;
import com.lx.mapper.QueryMapper;
import com.lx.util.LX;
import com.pdd.pop.sdk.http.PopClient;
import com.pdd.pop.sdk.http.PopHttpClient;
import com.pdd.pop.sdk.http.api.request.*;
import com.pdd.pop.sdk.http.api.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 创建人:游林夕/2019/5/19 19 22
 */
@Service("pinDuoDuoService")
public class PinDuoDuoService {
    private final PopClient client = new PopHttpClient("c873350cba6744f4b6e68e0e3421b5ed", "efe9397c4a7c268ca798989788563c881df66ce7");
//    private final String PID = "8689009_67264566";
    @Autowired
    private QueryMapper queryMapper;
    private final long DDK = 8689009L;
    @Autowired
    private RedisUtil redis;
    public static void main(String[] args) throws Exception {
//        String url = "https://mobile.yangkeduo.com/goods1.html?goods_id=5809338618&page_from=35&share_uin=NO7UWFTPTG6JNCXOVLQXSQPL6Y_GEXDA&refer_share_id=8a9d9da829b84b749836d42eb4450796&refer_share_uid=8865706396&refer_share_channel=copy_link";
//        System.out.println(url.substring(url.indexOf("goods_id=")+9,url.indexOf("&",url.indexOf("goods_id"))));
        //"2019-05-20 00:00:00","2019-05-25 00:00:00"
//                new PinDuoDuoService().urlToYhq(url);
//        System.out.println(System.currentTimeMillis());//1558713600000
//        new PinDuoDuoService().getOrder();
    }
//    @PostConstruct
    public void getOrder() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String time =redis.get("app:pdd:time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    long t1 = sdf.parse(time).getTime();
                    long bc = 1000L*60*60*24;
                    while (true){
                        System.out.println(time);
                        if (t1+bc>new Date().getTime()){//大于当前时间
                            Thread.sleep(bc);
                            continue;
                        }
                        try{
                            List<PddDdkOrderListRangeGetResponse.OrderListGetResponseOrderListItem> ls = getOrder(time,time=sdf.format(new Date(t1+=bc)));
                            redis.put("app:pdd:time",time);
                            if (LX.isNotEmpty(ls)){
                                for (PddDdkOrderListRangeGetResponse.OrderListGetResponseOrderListItem it : ls){
                                    Map map = LX.toMap("{'商品ID'='{0}','推广位ID'='{1}','淘宝订单编号'='{2}','付款预估收入'='{3}'}"
                                            ,it.getGoodsId(),it.getPId(),it.getOrderSn(),it.getPromotionAmount()/100.0);
                                    if (1==it.getOrderStatus()){//已付款
                                        map.put("订单状态","已付款");
                                    }else if(5==it.getOrderStatus()){//已结算
                                        map.put("订单状态","已结算");
                                    }
                                    queryMapper.updateOrderInfo(map);
                                }
                            }
                        }catch (Exception e){
                            Thread.sleep(1000L*60*5);
                            MyWxBot.log.error("pdd",e);
                        }
                    }
                }catch (Exception e){
                    MyWxBot.log.error("pdd",e);
                }
            }
        }).start();

    }
    private List<PddDdkOrderListRangeGetResponse.OrderListGetResponseOrderListItem> getOrder(String s1,String s2) throws Exception {
        PddDdkOrderListRangeGetRequest request = new PddDdkOrderListRangeGetRequest();
        request.setStartTime(s1);
        request.setPageSize(300);
        request.setEndTime(s2);
        PddDdkOrderListRangeGetResponse response = client.syncInvoke(request);
        if (LX.isNotEmpty(response)&&LX.isNotEmpty(response.getOrderListGetResponse())){
            return response.getOrderListGetResponse().getOrderList();
        }
        return null;
    }

    //说明:推广红包链接
    /**{ ylx } 2019/5/19 19:29 */
    public TGRespose urlToYhq(String url,String PID) throws Exception {
        PddDdkGoodsZsUnitUrlGenRequest request = new PddDdkGoodsZsUnitUrlGenRequest();
        request.setSourceUrl(url);
        request.setPid(PID);
        PddDdkGoodsZsUnitUrlGenResponse response = client.syncInvoke(request);
        LX.exObj(response,"未找到.该链.接的拼.多.多优.惠.券!");
        PddDdkGoodsZsUnitUrlGenResponse.GoodsZsUnitGenerateResponse res = response.getGoodsZsUnitGenerateResponse();
        PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem goods = urlToGoods(Long.valueOf(url.substring(url.indexOf("goods_id=")+9,url.indexOf("&",url.indexOf("goods_id=")))),PID);
        if (LX.isEmpty(goods)) return null;
        TGRespose t =  new TGRespose(goods.getGoodsImageUrl(), res.getShortUrl()
                ,goods.getGoodsId()+"",goods.getGoodsName(),""
                ,LX.getBigDecimal(goods.getMinGroupPrice()).divide(LX.getBigDecimal(100))
                ,LX.getBigDecimal(goods.getPromotionRate()));
        t.setText("总.价:"+t.getTotalPay()+"元,返.利:"+t.getFx()+"元;");
        t.setType(1);
        return t;
    }
    //说明:获取商品及佣金信息
    /**{ ylx } 2019/5/20 18:49 */
    public PddDdkGoodsSearchResponse.GoodsSearchResponseGoodsListItem urlToGoods(long gid,String PID) throws Exception {
        PddDdkGoodsSearchRequest request = new PddDdkGoodsSearchRequest();
        List<Long> goodsIdList = new ArrayList<Long>();
        goodsIdList.add(gid);
        request.setGoodsIdList(goodsIdList);
        request.setPid(PID);
        request.setCustomParameters("str");
        PddDdkGoodsSearchResponse p = client.syncInvoke(request);
        if (LX.isNotEmpty(p)&&LX.isNotEmpty(p.getGoodsSearchResponse().getGoodsList())){
            return p.getGoodsSearchResponse().getGoodsList().get(0);
        }else{
            return null;
        }
    }
}
