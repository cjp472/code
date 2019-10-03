package com.lx.orderPlatform.service.pay;

import com.fh.util.PageData;
import com.lx.orderPlatform.util.Tools;
import com.lx.util.LX;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:游林夕
 * @Date: 2018/10/22 8:49
 * Description:
 */
@Service("payService")
public class PayService implements OnlinePay{
    @Autowired
    private Tools tools;

    public OnlinePay getPayService(PageData pd) throws Exception {
        LX.exMap(pd,"payType");
        switch (pd.getStr("payType")){
            case "3":
            case "m":
                return tools.getBean("alipayService",OnlinePay.class);
            case "6":
            case "n":
                return tools.getBean("weixinService",OnlinePay.class);
            default:
                throw new Exception("线上只支持支付宝微信支付");
        }
    }
    @Override
    public PageData getPayInfo(PageData pd) throws Exception {
        return getPayService(pd).getPayInfo(pd);
    }

    @Override
    public PageData reconciliation(PageData pd) throws Exception {
        return getPayService(pd).reconciliation(pd);
    }

    @Override
    public PageData tradeRefund(PageData pd) throws Exception {
        LX.exMap(pd,"OrderNo,refundAmount");
        return getPayService(pd).tradeRefund(pd);
    }
    @Override
    public PageData findPayConfig(PageData pd) throws Exception {
        return null;
    }
    @Override
    public PageData getQRcode(PageData pd) throws Exception {
        return getPayService(pd).getQRcode(pd);
    }

    @Override
    public PageData facePayment(PageData pd) throws Exception {
        return getPayService(pd).facePayment(pd);
    }

    @Override
    public PageData orderQuery(PageData pd) throws Exception {
        return getPayService(pd).orderQuery(pd);
    }

}
