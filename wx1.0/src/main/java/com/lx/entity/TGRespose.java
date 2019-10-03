package com.lx.entity;//说明:

import com.lx.util.LX;

import java.math.BigDecimal;

/**
 * 创建人:游林夕/2019/5/19 19 27
 */
public class TGRespose {
    //商品类型 默认淘宝0 拼多多1
    private int type =0;
    //商品图片链接,pc链接,淘口令,商品ID,商品标题,优惠信息
    private String id,orderNo,status,add_time,is_enabled,user_id,p_user_id,imgUrl,url,numIid,title,text,qurl;
    //商品总价,佣金比例
    private BigDecimal totalPay,fx,yj;

    public TGRespose(String imgUrl, String url, String numIid, String title, String text, BigDecimal t, BigDecimal y) {
        this.imgUrl = imgUrl;
        this.url = url;
        this.numIid = numIid;
        this.title = title;
        this.text = text;
        this.totalPay = t==null?LX.getBigDecimal(0):t;
        this.yj = totalPay.multiply(y==null?LX.getBigDecimal(0):y).divide(LX.getBigDecimal(1000),2, BigDecimal.ROUND_HALF_DOWN);
        this.fx = this.yj.multiply(LX.getBigDecimal(75)).divide(LX.getBigDecimal(100),2, BigDecimal.ROUND_HALF_DOWN);
    }

    public String getQurl() {
        return qurl;
    }

    public void setQurl(String qurl) {
        this.qurl = qurl;
    }

    public String getP_user_id() {
        return p_user_id;
    }

    public void setP_user_id(String p_user_id) {
        this.p_user_id = p_user_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", status='" + status + '\'' +
                ", add_time='" + add_time + '\'' +
                ", is_enabled='" + is_enabled + '\'' +
                ", user_id='" + user_id + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", url='" + url + '\'' +
                ", numIid='" + numIid + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", totalPay=" + totalPay +
                ", fx=" + fx +
                ", yj=" + yj +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public String getIs_enabled() {
        return is_enabled;
    }

    public void setIs_enabled(String is_enabled) {
        this.is_enabled = is_enabled;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNumIid() {
        return numIid;
    }

    public void setNumIid(String numIid) {
        this.numIid = numIid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }

    public BigDecimal getFx() {
        return fx;
    }

    public void setFx(BigDecimal fx) {
        this.fx = fx;
    }

    public BigDecimal getYj() {
        return yj;
    }

    public void setYj(BigDecimal yj) {
        this.yj = yj;
    }
}
