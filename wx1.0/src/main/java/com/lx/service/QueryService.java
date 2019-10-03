package com.lx.service;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.entity.TGRespose;
import com.lx.entity.WxMessage;
import com.lx.mapper.QueryMapper;
import com.lx.util.LX;
import com.lx.util.MathUtil;
import com.lx.util.QRCodeMax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

/**
 * 创建人:游林夕/2019/6/6 17 50
 */
@Service
public class QueryService {
    @Autowired
    private QueryMapper queryMapper;
    @Autowired
    private PinDuoDuoService pddService;
    @Autowired
    private TaoBaoService taoBaoService;
    @Autowired
    private RedisUtil redisUtil;

    String patten = ".*(￥|₤|\\$|€|¢|https://m.tb.cn|描述.*后到|https://mobile\\.yangkeduo\\.com).*";
    public List<String> wx_in(WxMessage m)throws Exception {
        if (LX.isNotEmpty(m) && LX.isNotEmpty(m.getFromRemarkName())) {//消息不为空 群聊过滤
            if (m.getText().matches(patten)) {//符合购物
                return gw(m);
            } else {
                return exec(m);//用户输入其他
            }
        }
        return null;
    }
    public TGRespose tbgw(WxMessage m) throws Exception {
        String pid ="0";
        String text = m.getText();
        TGRespose tg=null;
        long item_id = taoBaoService.getID(text);//获取商品id
        Map map = queryMapper.queryPID(LX.toMap("{user_id='{0}',item_id='{1}',type='tb'}",m.getFromRemarkName(),item_id));
        LX.exMap(map,"pid");//获取推广位信息不存在
        pid =map.get("pid").toString();//推广位
        tg = taoBaoService.findFL(text,item_id,Long.parseLong(map.get("pid").toString()));
        LX.exObj(tg,"没有查询到优惠信息!");
        tg.setUser_id(m.getFromRemarkName());
        queryMapper.saveQueryInfo(LX.toMap("{user_id='{0}',item_id='{1}',title='{2}',pid={3},user_name='{4}',p_user_id='{5}',fx={6}}"
                ,m.getFromRemarkName(),tg.getNumIid(),tg.getTitle(),pid,m.getFromNickName(),m.getFromRemarkName().contains("M_")?m.getFromRemarkName().substring(13):"",tg.getFx()));
        return tg;
    }
    //购物
    public List<String> gw(WxMessage m) throws Exception {
        List<String> ls = new ArrayList<>();
        try{
            String pid ="0";
            String text = m.getText();
            TGRespose tg=null;
            if (text.startsWith("https://mobile.yangkeduo.com/")){//拼多多优惠券
                String url = text;
                long item_id=Long.valueOf(url.substring(url.indexOf("goods_id=")+9,url.indexOf("&",url.indexOf("goods_id="))));
                Map map = queryMapper.queryPID(LX.toMap("{user_id='{0}',item_id='{1}',type='pdd'}",m.getFromRemarkName(),item_id));
                LX.exMap(map,"pid");//获取推广位信息不存在
                pid = map.get("pid").toString();//推广位
                tg = pddService.urlToYhq(text,pid+"");
            }else{//淘宝
                long item_id = taoBaoService.getID(text);//获取商品id
                Map map = queryMapper.queryPID(LX.toMap("{user_id='{0}',item_id='{1}',type='tb'}",m.getFromRemarkName(),item_id));
                LX.exMap(map,"pid");//获取推广位信息不存在
                pid =map.get("pid").toString();//推广位
                tg = taoBaoService.findFL(text,item_id,Long.parseLong(map.get("pid").toString()));
            }
            LX.exObj(tg,"没有查询到优惠信息!");
            tg.setUser_id(m.getFromRemarkName());
            Map map = queryMapper.saveQueryInfo(LX.toMap("{user_id='{0}',item_id='{1}',title='{2}',pid={3},user_name='{4}',p_user_id='{5}',fx={6}}"
                    ,m.getFromRemarkName(),tg.getNumIid(),tg.getTitle(),pid,m.getFromNickName(),m.getFromRemarkName().contains("M_")?m.getFromRemarkName().substring(13):"",tg.getFx()));
            if (tg.getUrl().startsWith("http")){
                String out=" ◇ ◇ ◇省 ($ _ $) 消 息◇ ◇ ◇    \n" +
                        "\n"+tg.getTitle()+
                        "\n"+tg.getText()+
                        "\n优惠地址:"+tg.getUrl();
                ls.add(out);//返回
            }else{
                String uuid = LX.uuid32(5);
                redisUtil.put("app:gw:"+uuid,LX.toMap("{imgUrl='{0}',tkl='{1}'}"
                        ,tg.getImgUrl(),tg.getUrl()),2*24*60*60);
                ls.add(//"请输入\"教学\"查看新版教程\n" +
                        "http://52ylx.cn/h/"+uuid+"\n"+
                        "请点击上面的链接,直接购买\n" +
                        //"不要加入购物车或浏览其他商品\n" +
                        //"[红包](防止丢单!!!)[红包]\n"+
                        tg.getText()
                );
            }
        }catch (Exception e){
            MyWxBot.log.error("获取优惠信息",e);
            ls = new ArrayList<>();
            ls.add("一一一一返 丽 消 息一一一一\n" +
                    "[玫瑰]该商,品没有设置优~惠`券和佣!金");
        }
        return ls;
    }
    //微信其他消息
    public List<String> exec(WxMessage m) throws Exception {
        List<String> ls = new ArrayList<>();
        if (m.getText().startsWith("歌曲")){
            ls.add(getSong(m.getText().substring(2)));
            return ls;
        }
        String out = "";
        switch (m.getText()) {
            case "查询":
                Map mm = LX.toMap("{user_id='{0}'}",m.getFromRemarkName());
                List<Map<String,String>> list = queryMapper.findDDMX(mm);
                if (LX.isEmpty(list)){
//                    ls.add("付[款]和收[货]之后,第二天早上会通知您的哦!");
                    ls.add("没有查询到付[款]信息哦![付]款成功会主动推送给您的!");
                }else{
                    try {
                        mm.put("ls",list);
                        ls.add(QRCodeMax.creatDD(mm));
                    }catch (Exception e){
                        ls.add("查询出现错误!正在修复!");
                        MyWxBot.log.error("查询订单",e);
                    }
                }
                break;
            case "提现":
                    Map map = queryMapper.findTXInfo(LX.toMap("{user_id='{0}',user_name='{1}'}",m.getFromRemarkName(),m.getFromNickName()));
                    LX.exMap(map,"wtxje");//判断是否有未提现金额
                    if (LX.compareTo(map.get("wtxje"),"0", MathUtil.Type.GT)){//大于5元
                        ls.add(QRCodeMax.creatQTX(map));
                    }else{
                        ls.add("已确认收货:"+map.get("wtxje")+"元,不能提现的哦!"
//                                +"付[款]和收[货]之后,第二天早上会通知您的哦!"
                        );
                    }
                break;
            case "教学":
                ls.add(System.getProperty("user.dir")+"\\wx\\"+"jiaocheng.jpg");
                out=
                        "\n一一一一一温馨提示一一一一\n" +
//                        "付[款]和收[货]之后,第二天早上会通知您的哦!\n" +
//                        "使用了红包[淘]金币会减少反利的" +
//                        "[玫瑰][玫瑰][玫瑰]请谅解![玫瑰][玫瑰][玫瑰]\n"+
                        "回复“提现”可以提取当前余.额\n" +
                        "回复“查询”可以获取账.单信息\n" ;
                break;
            case "tgUrl":
                out=taoBaoService.getTGUrl();
                break;
        }
        ls.add(out);
        return ls;
    }


    //获取歌曲
    public String getSong(String name) throws Exception {
        try {
            String s = LX.doPost("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword="+ URLEncoder.encode(name,"utf-8")+"&page=1&pagesize=3&showtype=1", null);
            HashMap map = LX.toMap(HashMap.class,s);
            List<HashMap> ls = (List<HashMap>) ((HashMap)map.get("data")).get("info");
            StringBuilder sb = new StringBuilder();
            for (HashMap m : ls){
                String hash = (String) m.get("hash");
                String s1 = LX.doPost("http://m.kugou.com/app/i/getSongInfo.php?cmd=playInfo&hash="+hash,null);
                HashMap mp3_m = LX.toMap(HashMap.class,s1);
                if (LX.isNotEmpty(mp3_m)){
                    sb.append("\n"+mp3_m.get("singerName")+"-"+mp3_m.get("songName")+" 地址:\n")
                            .append(mp3_m.get("url"));
                }
            }
            return sb.toString();
        }catch (Exception e){
        }
        return "";
    }
}
