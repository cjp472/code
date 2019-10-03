package com.lx.orderPlatform.service.pay.wxpay;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.fh.util.PageData;
import com.lx.util.LX;

public class WXPayConfigImpl extends WXPayConfig {
	private PageData map ;
	private String wxzsAddress ="";

    public WXPayConfigImpl(){
        try {
            wxzsAddress = LX.loadProperties("application-other","wxzsAddress");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	public void setMap(PageData map) {
		this.map = map;
	}

	@Override
	String getAppID() {
		return map.getString("WXAppid");
	}

	@Override
	String getMchID() {
		return map.getString("WXMchID");
	}

	@Override
	String getKey() {
		return map.getString("WXKey");
	}
	@Override
	InputStream getCertStream() {
        if (LX.isEmpty(this.wxzsAddress)){//读取本地resource里的证书
            String path = "WXZS/wx"+getMchID()+"/apiclient_cert.p12";
            return WXPayConfigImpl.class.getClassLoader().getResourceAsStream(path);
        }else{//读取指定文件夹里的
            try {
                String path = "file:///"+this.wxzsAddress+"WXZS/wx"+getMchID()+"/apiclient_cert.p12";
                URL url = new URL(path);
                return url!=null?url.openStream():null;
            } catch (Exception e) {
                return null;
            }
        }
	}

	@Override
	IWXPayDomain getWXPayDomain() {
		return new IWXPayDomain(){

			@Override
			public void report(String domain, long elapsedTimeMillis, Exception ex) {
			}

			@Override
			public DomainInfo getDomain(WXPayConfig config) {
				return new DomainInfo("api.mch.weixin.qq.com", false);
			}
			
		};
	}

}
