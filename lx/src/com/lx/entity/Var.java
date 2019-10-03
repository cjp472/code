package com.lx.entity;

import com.lx.util.LX;

import java.util.*;

/**
 * Created by 游林夕 on 2019/8/23.
 */
public class Var extends HashMap {
    public Var(){}
    public Var(Map map){this.putAll(map);}
    public Var(Object k,Object v){this.put(k,v); }
    public Var(Object[][] kv){
        if(LX.isEmpty(kv)) return;
        for (Object[] os : kv){
            if (os.length!=2) LX.exMsg("内部数组长度不为2");
            this.put(os[0],os[1]);
        }
    }
    public Var(String str ,Object...v){this.putAll(LX.toMap(str,v));}
    /** 获取字符串类型*/
    public String getStr(Object key){return LX.isEmpty(get(key))?"":get(key).toString();}
    public List getList(Object key){return getObj(List.class,key);}
    public Var getVar(Object key){return getObj(Var.class,key);}
    /** 获取值(可能强转不了)*/
    public <T> T getObj(Object key){return (T) get(key);}
    /** 获取指定类型值*/
    public <T> T getObj(Class<T> t,Object key){return LX.toObj(t,get(key));}


}
