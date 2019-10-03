package com.lx.entity;//说明:

import java.math.BigDecimal;

/**
 * 创建人:游林夕/2019/5/31 22 13
 */
public class Fxmx {
    private String id,user_id,add_time;
    private BigDecimal fx;

    public Fxmx(String id, String user_id, String add_time, BigDecimal fx) {
        this.id = id;
        this.user_id = user_id;
        this.add_time = add_time;
        this.fx = fx;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAdd_time() {
        return add_time;
    }

    public void setAdd_time(String add_time) {
        this.add_time = add_time;
    }

    public BigDecimal getFx() {
        return fx;
    }

    public void setFx(BigDecimal fx) {
        this.fx = fx;
    }
}
