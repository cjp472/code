package com.lx.orderPlatform.service.pay;


import com.fh.util.PageData;

/**
 * 支付基类
 * @author Administrator
 *
 */
public interface OnlinePay {
    /**
     * 获取app缴费码
     * @param pd
     * @return
     * @throws Exception
     */
    PageData getPayInfo(PageData pd) throws Exception;
    /**
     * 下载对账单
     * @param pd
     * @return
     * @throws Exception
     */
    PageData reconciliation(PageData pd) throws Exception;
    /**
     * 执行退款
     * @param pd
     * @return
     * @throws Exception
     */
    PageData tradeRefund(PageData pd) throws Exception;
    /**
     * 获取支付的配置信息
     *  @author
     *  创建时间：2018年3月1日 上午10:54:26
     */
    PageData findPayConfig(PageData pd) throws Exception;
    /**
     * 获取二维码信息
     *  @author
     *  创建时间：2018年3月1日 上午10:54:26
     */
    PageData getQRcode(PageData pd) throws Exception;

    /**当面付*/
    PageData facePayment(PageData pd) throws Exception;
    /**查询订单*/
    PageData orderQuery(PageData pd) throws Exception;
}
