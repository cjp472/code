package com.lx.service;//说明:

import com.lx.role.dao.RedisUtil;
import com.lx.entity.WxMessage;
import com.lx.util.LX;
import io.github.biezhi.wechat.api.enums.AccountType;
import io.github.biezhi.wechat.api.model.Account;
import io.github.biezhi.wechat.api.model.Recommend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 创建人:游林夕/2019/5/28 19 09
 */
@Service
public class MyWxBot extends WxBot {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private QueryService queryService;

    public static void main(String [] args){
        MyWxBot b = new MyWxBot();
    }
    @Override
    protected void addFriend(Recommend r) {
        if (!r.getContent().startsWith("lxzz")) {//不是已这个开头
            verify(r);//添加好友
            Account a = new Account();
            a.setUserName(r.getUserName());
            a.setNickName(r.getNickName());
            a.setAccountType(AccountType.TYPE_FRIEND);
            accountHashMap.put(a.getUserName(),a);
            remarkName(a.getUserName());//修改备注
            getText(new WxMessage("教学",a.getUserName(),a.getNickName(),a.getRemarkName(),""));//发送信息
        }
    }
    //修改备注名
    public String remarkName(String name){
        Account a = accountHashMap.get(name);
        String rName = a.getRemarkName();
        if (LX.isEmpty(rName)){//不是自定义开头
//            String pName = rName.startsWith("M_lxzz")?rName.substring(2):"";
            rName = "lxzz"+redisUtil.getId("app:user:id",6);
//            if (LX.isNotEmpty(pName)){//有推荐人
//                rName = rName+"_M_"+pName;
//                send(pName,"您的好友["+a.getNickName()+"]已成功添加!");
//            }
            rName(a.getUserName(),rName);//修改备注名
            a.setRemarkName(rName);//修改系统中的备注名
        }
        //通过备注找用户
        rnameToName.put(rName,name);
        return a.getRemarkName();
    }
    public void send(String rname ,String s){
        try {
            if (s.endsWith("jpg")||s.endsWith("png")){
                sendImg(rnameToName.get(rname),s);
            }else{
                sendText(rnameToName.get(rname),s);
            }
        }catch (Exception e){
            log.error("根据备注发消息",e);
        }
    }
    @Override
    protected void getText(WxMessage msg) {
        try{
            Account a = accountHashMap.get(msg.getFromUserName());
            if (a!=null){
                msg.setFromNickName(a.getNickName());
                msg.setFromRemarkName(a.getRemarkName());
                String r = remarkName(msg.getFromUserName());
                if (r == null) return;
                msg.setFromRemarkName(r);//修改信息备注名
            }else{
                msg.setText(msg.getText().substring(msg.getText().indexOf("<br/>")+5));
            }
            log.info("收到消息:"+msg.getFromNickName()+"   "+msg.getText());
            //发送消息到我的账号
            List<String> ls = queryService.wx_in(msg);
            if (LX.isNotEmpty(ls)){
                for(String s : ls){
                    if (LX.isEmpty(s)) continue;
                    if (s.endsWith("jpg")||s.endsWith("png")){
                        if ("提现".equals(msg.getText())){//发送给晓贴
                            sendImg(rnameToName.get("lxzz000002"),s);
                        }
                        sendImg(msg.getFromUserName(),s);
                    }else{
                        sendText(msg.getFromUserName(),s);
                    }
                }
            }
        }catch (Exception e){
            log.error("收到消息",e);
            sendText(msg.getFromUserName(),"对不起!我出现问题了!正在修复....");
        }
    }
}
