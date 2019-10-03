package com.lx.entity;//说明:

/**
 * 创建人:游林夕/2019/5/21 19 31
 */
public class TGRequest {
    private String id,name,msg;

    public TGRequest(String id,String name, String msg) {
        this.name = name;
        this.msg = msg;
        this.id=id;
    }

    @Override
    public String toString() {
        return "{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", msg='" + msg + '\'' +
                '}';
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
