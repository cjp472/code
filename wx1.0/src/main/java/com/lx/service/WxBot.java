package com.lx.service;//说明:

import com.google.gson.reflect.TypeToken;
import com.lx.entity.WxMessage;
import com.lx.util.LX;
import io.github.biezhi.wechat.api.client.BotClient;
import io.github.biezhi.wechat.api.constant.Constant;
import io.github.biezhi.wechat.api.enums.AccountType;
import io.github.biezhi.wechat.api.enums.RetCode;
import io.github.biezhi.wechat.api.model.*;
import io.github.biezhi.wechat.api.request.BaseRequest;
import io.github.biezhi.wechat.api.request.FileRequest;
import io.github.biezhi.wechat.api.request.JsonRequest;
import io.github.biezhi.wechat.api.request.StringRequest;
import io.github.biezhi.wechat.api.response.*;
import io.github.biezhi.wechat.exception.WeChatException;
import io.github.biezhi.wechat.utils.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创建人:游林夕/2019/5/28 13 42
 */
public abstract class WxBot {
    public WxBot(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    start();
                } catch (Exception e) {
                    log.error("启动",e);
                }
            }
        }).start();
    }
    private static final Pattern UUID_PATTERN = Pattern.compile("window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";");
    private static final Pattern CHECK_LOGIN_PATTERN = Pattern.compile("window.code=(\\d+)");
    private static final Pattern PROCESS_LOGIN_PATTERN = Pattern.compile("window.redirect_uri=\"(\\S+)\";");
    private static final Pattern SYNC_CHECK_PATTERN = Pattern.compile("window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"}");
    static Logger log = LoggerFactory.getLogger(WxBot.class);
    protected BotClient client = new BotClient(getClient());//客户端
    private LoginSession session = new LoginSession();//登陆信息
    protected String dir = System.getProperty("user.dir");//地址
    private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue();//消息队列
    protected HashMap<String,Account> accountHashMap = new HashMap<>(1024);//存好友信息
    protected Map<String,String> rnameToName = new HashMap<>();

    private void start() throws Exception {
        autoLogin();//登陆
        init();//初始化信息
        addMsg();//添加监听
        takeMsg();//消费消息
        reflogin();//定时登陆
    }

    private void reflogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try{
                        try {
                            Thread.sleep(6*60*60*1000);//6个小时
                            autoLogin();//登陆
                            init();//初始化信息
                        }catch (Exception e1){
                            Thread.sleep(60*1000);
                            autoLogin();//登陆
                            init();//初始化信息
                        }
                    }catch (Exception e){
                        LX.sleep(30000);
                    }
                }
            }
        }).start();
    }

    public boolean sendImg(String toUserName, String filePath) {
        String mediaId = this.uploadMedia(toUserName, filePath).getMediaId();
        if (StringUtils.isEmpty(mediaId)) {
            log.info("Media为空");
            return false;
        } else {
            String url = String.format("%s/webwxsendmsgimg?fun=async&f=json&pass_ticket=%s", session.getUrl(), session.getPassTicket());
            String msgId = System.currentTimeMillis()+"" +(new Random().nextInt(9000)+1000);
            Map<String, Object> msg = new HashMap();
            msg.put("Type", 3);
            msg.put("MediaId", mediaId);
            msg.put("FromUserName", session.getUserName());
            msg.put("ToUserName", toUserName);
            msg.put("LocalID", msgId);
            msg.put("ClientMsgId", msgId);
            JsonResponse response = (JsonResponse)client.send(((JsonRequest)((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody())
                    .add("BaseRequest", session.getBaseRequest())).add("Msg", msg));
            return null != response && response.success();
        }
    }
    public MediaResponse uploadMedia(String toUser, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("文件[" + filePath + "]不存在");
        } else {
            log.info("开始上传文件: {}"+filePath);
            long size = file.length();
            String mimeType = WeChatUtils.getMimeType(filePath);
            String mediatype = "pic";
            String url = String.format("%s/webwxuploadmedia?f=json", session.getFileUrl());
            String mediaId = System.currentTimeMillis() +"" +(new Random().nextInt(9000)+1000);
            Map<String, Object> uploadMediaRequest = new HashMap(10);
            uploadMediaRequest.put("UploadType", 2);
            uploadMediaRequest.put("BaseRequest", session.getBaseRequest());
            uploadMediaRequest.put("ClientMediaId", mediaId);
            uploadMediaRequest.put("TotalLen", size);
            uploadMediaRequest.put("StartPos", 0);
            uploadMediaRequest.put("DataLen", size);
            uploadMediaRequest.put("MediaType", 4);
            uploadMediaRequest.put("FromUserName", session.getUserName());
            uploadMediaRequest.put("ToUserName", toUser);
            uploadMediaRequest.put("FileMd5", MD5Checksum.getMD5Checksum(file.getPath()));
            String dataTicket = client.cookie("webwx_data_ticket");
            if (StringUtils.isEmpty(dataTicket)) {
                throw new WeChatException("缺少了附件Cookie");
            } else {
                ApiResponse response = this.client.send(((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)(new StringRequest(url)).post()).multipart()).fileName(file.getName()))
                        .add("id", "WU_FILE_1"))
                        .add("name", filePath))
                        .add("type", mimeType))
                        .add("lastModifieDate",(new SimpleDateFormat("yyyy MM dd HH:mm:ss")).format(new Date())))
                        .add("size", String.valueOf(size)))
                        .add("mediatype", mediatype))
                        .add("uploadmediarequest", WeChatUtils.toJson(uploadMediaRequest)))
                        .add("webwx_data_ticket", dataTicket)).add("pass_ticket", session.getPassTicket()))
                        .add("filename", RequestBody.create(MediaType.parse(mimeType), file)));
                MediaResponse mediaResponse = (MediaResponse)response.parse(MediaResponse.class);
                if (!mediaResponse.success()) {
                    log.info("上传附件失败:"+mediaResponse.getMsg());
                }

                log.info("文件上传成功:"+filePath);
                return mediaResponse;
            }
        }
    }
    

    //发信息
    protected boolean sendText(String toUserName, String msg) {
//        DateUtils.sendSleep();
        String url = String.format("%s/webwxsendmsg?pass_ticket=%s", session.getUrl(), session.getPassTicket());
        String msgId = System.currentTimeMillis() / 1000L + StringUtils.random(6);
        JsonResponse response = (JsonResponse)client.send(((JsonRequest)((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody()).add("BaseRequest", session.getBaseRequest())).add("Msg", new SendMessage(1, msg, session.getUserName(), toUserName, msgId, msgId)));
        return null != response && response.success();
    }
    //确认加好友
    protected  boolean verify(Recommend recommend) {
        String url = String.format("%s/webwxverifyuser?r=%s&lang=zh_CN&pass_ticket=%s", session.getUrl(), System.currentTimeMillis() / 1000L, session.getPassTicket());
        List<Map<String, Object>> verifyUserList = new ArrayList();
        Map<String, Object> verifyUser = new HashMap(2);
        verifyUser.put("Value", recommend.getUserName());
        verifyUser.put("VerifyUserTicket", recommend.getTicket());
        verifyUserList.add(verifyUser);
        JsonResponse response = (JsonResponse)client.send(((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody()).add("BaseRequest", session.getBaseRequest())).add("Opcode", 3)).add("VerifyUserListSize", 1)).add("VerifyUserList", verifyUserList)).add("VerifyContent", "")).add("SceneListCount", 1)).add("SceneList", Arrays.asList(33))).add("skey",session.getSyncKeyStr()));
        return null != response && response.success();
    }
    //获取消息
    protected  void webSync() {
        String url = String.format("%s/webwxsync?sid=%s&sKey=%s&passTicket=%s", session.getUrl(), session.getWxSid(), session.getSKey(), session.getPassTicket());
        JsonResponse response = (JsonResponse)client.send(((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody()).add("BaseRequest",session.getBaseRequest())).add("SyncKey", session.getSyncKey())).add("rr", ~(System.currentTimeMillis() / 1000L)));
        WebSyncResponse webSyncResponse = (WebSyncResponse)response.parse(WebSyncResponse.class);
        if (webSyncResponse.success()) {
            session.setSyncKey(webSyncResponse.getSyncKey());
            List<Message> ls = webSyncResponse.getAddMessageList();
//            log.info("监听消息..."+LX.toJSONString(ls));
            queue.addAll(ls);//存入队列
        }
    }
    //添加好友
    protected abstract void addFriend(Recommend r);
    //发送消息
    protected abstract void getText(WxMessage msg);
    //修改备注
    protected void rName(String name,String rName){
        String url = String.format("%s/webwxoplog?r=%s&lang=zh_CN&pass_ticket=%s", session.getUrl(), System.currentTimeMillis() / 1000L, session.getPassTicket());
        JsonRequest j = new JsonRequest(url)
                .post().jsonBody()
                .add("BaseRequest", session.getBaseRequest())
                .add("CmdId", 2)
                .add("RemarkName", rName)
                .add("UserName",name);
        JsonResponse response = (JsonResponse)client.send(j);
        boolean b = null != response && response.success();
        if (!b) LX.exMsg("修改备注名失败!");
    }
    //使用消息
    public void takeMsg(){
        for (int i=0;i<3;i++){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                        while (true){
                            try {
                                Message m = queue.take();
//                                log.info("消费信息..."+LX.toJSONString(m));
//                                Account a = accountHashMap.get(m.getFromUserName());
//                                if (a!=null&&a.getAccountType()!= AccountType.TYPE_FRIEND) continue;
                                switch(m.msgType()) {
                                    case TEXT:
//                                        if (a==null) break;
                                        WxMessage ww = new WxMessage(m.getContent(),m.getFromUserName(),"群聊","群聊","");
                                        getText(ww);
                                        break;
                                    case SHARE://链接
                                        WxMessage lj = new WxMessage(m.getUrl(),m.getFromUserName(),"群聊","群聊","");
                                        getText(lj);
                                        break;
                                    case ADD_FRIEND://加好友
                                        addFriend(m.getRecommend());
                                        break;
                                }
                            } catch (Exception e) {
                                log.error("消费端",e);
                            }
                        }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
    //监听消息
    public void addMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        LX.sleep(3000);
                        String url = String.format("%s/synccheck", session.getSyncOrUrl());
                        ApiResponse response = client.send(((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)(new StringRequest(url)).add("r", System.currentTimeMillis())).add("skey", session.getSKey())).add("sid", session.getWxSid())).add("uin", session.getWxUin())).add("deviceid", session.getDeviceId())).add("synckey", session.getSyncKeyStr())).add("_", System.currentTimeMillis())).timeout(30));
                        Matcher matcher = SYNC_CHECK_PATTERN.matcher(response.getRawBody());
                        SyncCheckRet s = null;
                        if (matcher.find()) {
                            if (!"0".equals(matcher.group(1))) {
                                s = new SyncCheckRet(RetCode.parse(Integer.valueOf(matcher.group(1))), 0);
                            } else {
                                s = new SyncCheckRet(RetCode.parse(Integer.valueOf(matcher.group(1))), Integer.valueOf(matcher.group(2)));
                            }
                        }
                        if (s.getRetCode() != RetCode.LOGIN_OTHERWISE) {//其他地方登陆
                            if (s.getRetCode() == RetCode.NORMAL) {
                                switch(s.getSelector()) {
                                    case 6:
                                    case 2:
                                        webSync();
                                        break;
                                }
                            }else{
                                Thread.sleep(30000);
                                autoLogin();//登陆
                                init();//初始化信息
                            }
                            continue;
                        }
                        new FileOutputStream(dir + "/login.json").write(3123);
                        log.info("出现错误停止!");
                        return;//结束
                    } catch (Exception e) {
                        try {
                            Thread.sleep(30000);
                            autoLogin();//登陆
                            init();//初始化信息
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        log.error("监听",e);
                    }
                }

            }
        }).start();
    }

    //6开始通知
    public void statusNotify() {
        String url = String.format("%s/webwxstatusnotify?lang=zh_CN&pass_ticket=%s", session.getUrl(), session.getPassTicket());
        client.send(((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody()).add("BaseRequest", session.getBaseRequest())).add("Code", 3)).add("FromUserName", session.getUserName())).add("ToUserName", session.getUserName())).add("ClientMsgId", System.currentTimeMillis() / 1000L));
    }
    //5保存到json
    public void updateLastCheck() {
        String file = dir + "/login.json";
        WeChatUtils.writeJson(file, HotReload.build(session));
    }

    //4.1获取所有好友
    public synchronized void getAllAccount(){
        log.info("微信获取所有好友...");
        int r = (int)(-System.currentTimeMillis() / 1000L) / 1579;
        String url = String.format("%s/webwxgetcontact?r=%s&seq=%s&skey=%s", session.getUrl(), System.currentTimeMillis(), 0, session.getSKey());
        JsonResponse jsonResponse = (JsonResponse)this.client.send((new JsonRequest(url)).jsonBody());
        List<Account> memberList = (List)WeChatUtils.fromJson(WeChatUtils.toJson(jsonResponse.toJsonObject().getAsJsonArray("MemberList")), new TypeToken<List<Account>>() {});
        log.info("获取到"+memberList.size()+"个好友!");
        for (Account a : memberList){
            accountHashMap.put(a.getUserName(),a);
            rnameToName.put(a.getRemarkName().startsWith("lxzz")?a.getRemarkName().substring(0,10):a.getRemarkName(),a.getUserName());
        }
    }
    //4初始化基础信息
    public void init(){
        log.info("微信初始化...");
        int r = (int)(-System.currentTimeMillis() / 1000L) / 1579;
        String url = String.format("%s/webwxinit?r=%d&pass_ticket=%s", session.getUrl(), r, session.getPassTicket());
        JsonResponse response = (JsonResponse)client.send(((JsonRequest)((JsonRequest)(new JsonRequest(url)).post()).jsonBody()).add("BaseRequest", session.getBaseRequest()));
        WebInitResponse webInitResponse = (WebInitResponse)response.parse(WebInitResponse.class);
        List<Account> contactList = webInitResponse.getContactList();
        Account account = webInitResponse.getAccount();
        SyncKey syncKey = webInitResponse.getSyncKey();
        session.setInviteStartCount(webInitResponse.getInviteStartCount());
        session.setAccount(account);
        session.setUserName(account.getUserName());
        session.setNickName(account.getNickName());
        session.setSyncKey(syncKey);
        updateLastCheck();
        statusNotify();
        getAllAccount();
        log.info("微信初始化成功...");
    }
    //将登陆信息放入session
    private boolean processLoginSession(String loginContent) {
        Matcher matcher = PROCESS_LOGIN_PATTERN.matcher(loginContent);
        if (matcher.find()) {
            session.setUrl(matcher.group(1));
        }
        ApiResponse response = client.send((new StringRequest(session.getUrl())).noRedirect());
        session.setUrl(session.getUrl().substring(0, session.getUrl().lastIndexOf("/")));
        String body = response.getRawBody();
        List<String> fileUrl = new ArrayList();
        List<String> syncUrl = new ArrayList();
        for(int i = 0; i < Constant.FILE_URL.size(); ++i) {
            fileUrl.add(String.format("https://%s/cgi-bin/mmwebwx-bin", Constant.FILE_URL.get(i)));
            syncUrl.add(String.format("https://%s/cgi-bin/mmwebwx-bin", Constant.WEB_PUSH_URL.get(i)));
        }
        boolean flag = false;
        for(int i = 0; i < Constant.FILE_URL.size(); ++i) {
            String indexUrl = (String) Constant.INDEX_URL.get(i);
            if (session.getUrl().contains(indexUrl)) {
                session.setFileUrl((String)fileUrl.get(i));
                session.setSyncUrl((String)syncUrl.get(i));
                flag = true;
                break;
            }
        }
        if (!flag) {
            session.setFileUrl(session.getUrl());
            session.setSyncUrl(session.getUrl());
        }
        session.setDeviceId("e" + String.valueOf(System.currentTimeMillis()));
        BaseRequest baseRequest = new BaseRequest();
        session.setBaseRequest(baseRequest);
        session.setSKey(WeChatUtils.match("<skey>(\\S+)</skey>", body));
        session.setWxSid(WeChatUtils.match("<wxsid>(\\S+)</wxsid>", body));
        session.setWxUin(WeChatUtils.match("<wxuin>(\\S+)</wxuin>", body));
        session.setPassTicket(WeChatUtils.match("<pass_ticket>(\\S+)</pass_ticket>", body));
        baseRequest.setSkey(session.getSKey());
        baseRequest.setSid(session.getWxSid());
        baseRequest.setUin(session.getWxUin());
        baseRequest.setDeviceID(session.getDeviceId());
        return true;
    }
    //3扫码
    public void sweepCode(String uuid) throws Exception {
        String url = String.format("%s/cgi-bin/mmwebwx-bin/login", "https://login.weixin.qq.com");
        for (int i = 0;i<100;i++){
            Long time = System.currentTimeMillis();
            ApiResponse response = client.send(((StringRequest)((StringRequest)((StringRequest)((StringRequest)((StringRequest)(new StringRequest(url)).add("loginicon", true)).add("uuid", uuid)).add("tip", "1")).add("_", time)).add("r", (int)(-time / 1000L) / 1579)).timeout(30));
            Matcher matcher = CHECK_LOGIN_PATTERN.matcher(response.getRawBody());
            if (matcher.find()) {
                if ("200".equals(matcher.group(1))) {
                    log.info("登陆成功!");
                    processLoginSession(response.getRawBody());
                    return;
                }
            } else {
                LX.exMsg("登陆失败!");
            }
            Thread.sleep(3000);
        }
        LX.exMsg("登陆超时!");
    }
    //2显示二维码
    public void showQRcode() throws Exception {
        //uuid
        String uuid = null;
        ApiResponse response = client.send(((StringRequest)(new StringRequest("https://login.weixin.qq.com/jslogin")).add("appid", "wx782c26e4c19acffb")).add("fun", "new"));
        Matcher matcher = UUID_PATTERN.matcher(response.getRawBody());
        if (matcher.find() && "200".equals(matcher.group(1))) {
            uuid = matcher.group(2);
        }
        LX.exObj(uuid,"没有获取到UUID");
        //二维码
        String imgDir = dir;
        FileResponse fileResponse = (FileResponse)client.download(new FileRequest(String.format("%s/qrcode/%s", "https://login.weixin.qq.com", uuid)));
        InputStream inputStream = fileResponse.getInputStream();
        File qrCode = WeChatUtils.saveFile(inputStream, imgDir, "qrcode.png");
        log.info("二维码生成成功!");
        LX.sleep(3000L);
        //扫码
        sweepCode(uuid);
    }
    //1自动登陆
    private void autoLogin() throws Exception {
        String file = dir + "/login.json";
        try {//使用文件登陆
            HotReload hotReload = (HotReload) WeChatUtils.fromJson(new FileReader(file), HotReload.class);
            BotClient.recoverCookie(hotReload.getCookieStore());
            session = hotReload.getSession();
            return;
        } catch (Exception var3) {}
        showQRcode();//扫码登陆
    }

    private OkHttpClient getClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpUtils.configureToIgnoreCertificate(builder);
        return builder.build();
    }
}

