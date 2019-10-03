package com.lx.entity;//说明:

import com.lx.util.LX;

import java.math.BigDecimal;

/**
 * 创建人:游林夕/2019/5/21 20 01
 */
public class My_user {
    //主键,姓名,是否有效,添加时间,推荐ID
    private String id,name,is_enabled,add_time,tjid;
    //未结算金额,未提现金额,已提现金额,推广金额
    private BigDecimal wjsje,wtxje,ytxje,tgje;
    //推荐人数 有效推广
    private int tjrs,yxtg;

    public My_user(){}
    public My_user(String id,String name, String is_enabled ,String add_time,String tjid) {
        setTjid(tjid);
        setIs_enabled(is_enabled);
        setAdd_time(add_time);
        setName(name);
        setId(id);
        setWjsje(LX.getBigDecimal(0));
        setWtxje(LX.getBigDecimal(0));
        setYtxje(LX.getBigDecimal(0));
        setTgje(LX.getBigDecimal(0));
    }

    @Override
    public String toString() {
        return "My_user{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", is_enabled='" + is_enabled + '\'' +
                ", add_time='" + add_time + '\'' +
                ", tjid='" + tjid + '\'' +
                ", wjsje=" + wjsje +
                ", wtxje=" + wtxje +
                ", ytxje=" + ytxje +
                ", tgje=" + tgje +
                ", tjrs=" + tjrs +
                ", yxtg=" + yxtg +
                '}';
    }

    public BigDecimal getTgje() {
        return tgje;
    }

    public void setTgje(BigDecimal tgje) {
        this.tgje = tgje;
    }

    public int getTjrs() {
        return tjrs;
    }

    public void setTjrs(int tjrs) {
        this.tjrs = tjrs;
    }

    public int getYxtg() {
        return yxtg;
    }

    public void setYxtg(int yxtg) {
        this.yxtg = yxtg;
    }

    public String getTjid() {
        return tjid;
    }

    public void setTjid(String tjid) {
        this.tjid = tjid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIs_enabled() {
        return is_enabled;
    }

    public void setIs_enabled(String is_enabled) {
        this.is_enabled = is_enabled;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public BigDecimal getWjsje() {
        return wjsje;
    }

    public void setWjsje(BigDecimal wjsje) {
        this.wjsje = wjsje;
    }

    public BigDecimal getWtxje() {
        return wtxje;
    }

    public void setWtxje(BigDecimal wtxje) {
        this.wtxje = wtxje;
    }

    public BigDecimal getYtxje() {
        return ytxje;
    }

    public void setYtxje(BigDecimal ytxje) {
        this.ytxje = ytxje;
    }
}
