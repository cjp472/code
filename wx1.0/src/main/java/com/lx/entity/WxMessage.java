package com.lx.entity;

import java.io.Serializable;

public class WxMessage implements Serializable {
    private String text;
    private String fromUserName;
    private String fromNickName;
    private String fromRemarkName;
    private String img;
    private String tg;

    public WxMessage(String text, String fromUserName, String fromNickName, String fromRemarkName,String tg) {
        this.text = text;
        this.fromUserName = fromUserName;
        this.fromNickName = fromNickName;
        this.fromRemarkName = fromRemarkName;
        this.tg = tg;
    }

    @Override
    public String toString() {
        return "{" +
                "text='" + text + '\'' +
                ", fromUserName='" + fromUserName + '\'' +
                ", fromNickName='" + fromNickName + '\'' +
                ", fromRemarkName='" + fromRemarkName + '\'' +
                ", img='" + img + '\'' +
                '}';
    }

    public String getTg() {
        return tg;
    }

    public void setTg(String tg) {
        this.tg = tg;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getFromNickName() {
        return fromNickName;
    }

    public void setFromNickName(String fromNickName) {
        this.fromNickName = fromNickName;
    }

    public String getFromRemarkName() {
        return fromRemarkName;
    }

    public void setFromRemarkName(String fromRemarkName) {
        this.fromRemarkName = fromRemarkName;
    }
}
