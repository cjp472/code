package com.lx.util;//说明:



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import static com.lx.util.LX.isEmpty;
import static com.lx.util.LX.isNotEmpty;

/**
 * 创建人:游林夕/2019/3/27 15 01
 */
class CollectionUtil {
    public static void main(String [] args) throws Exception {
//        List<PageData> s = LX.toList(PageData[].class,LX.format("[{a={0},b='{1}'},{a={0},b='{1}'}]","{a='{0}',b='{1}'}","苏打水"));
        HashMap pd = LX.toMap(HashMap.class,"{a='阿迪达斯',b={0}}","{a={a=0},b='{1}'}");
        System.out.println(((HashMap)pd.get("b")).get("a"));

    }
    /**
     * 克隆Map
     */
    static <T extends Map>T cloneMap(T obj, String str) {
        if (isEmpty(obj))return null;
        if (isNotEmpty(str)){
            Map map = new HashMap();
            for (String key : str.split(",")){
                map.put(key,obj.get(key));
            }
            return (T) LX.toMap(obj.getClass(),deepClone(map));
        }else {
            return deepClone(obj);
        }
    }
    /**
     * 将对象Clone
     */
    static <T> T  deepClone(T obj){
        try{
            // 将对象写到流里
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            // 从流里读出来
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (T) oi.readObject();
        }catch (Exception e){
            return LX.exMsg(e);
        }
    }
    /**将对象转为对象*/
    static <T> T toObj(Class<T> t,Object obj){
        if (LX.isEmpty(obj)) return null;
        if (t.isAssignableFrom(obj.getClass())) return (T) obj;
        return g.fromJson(obj instanceof String?(String)obj:g.toJson(obj),t);
    }
    /**
     * 将对象转为Map
     *  @author
     *  创建时间：2018年4月26日 下午5:35:55
     */
    static <T extends Map>T toMap(Class<T> t,Object json) {
        if (LX.isEmpty(json)) return null;
        if (t.isAssignableFrom(json.getClass())) return (T) json;
        Map pd = g.fromJson(json instanceof String?(String)json:g.toJson(json),Map.class);
        T pp = null;
        try {
            pp = t.newInstance();
            for (Object key : pd.keySet()) {
                if(isEmpty(key)) continue;
                Object obj = pd.get(key);
                if(isNotEmpty(obj)){
                    if(obj instanceof List){
                        pp.put(key, toList(t,obj));
                    }else if(obj instanceof Map){
                        pp.put(key, toMap(t,g.toJson(obj)));
                    }else{
                        if (obj instanceof Number)
                            obj = (double)obj == (long)(double)obj?(long)(double)obj:obj;
                        pp.put(key,obj);
                    }
                }else{
                    pp.put(key, obj);
                }
            }
        } catch (Exception e) {
            LX.exMsg(e);
        }
        return pp;
    }
    /**将对象转为List*/
    static <T> List<T> toList(Class<T> t,Object obj){
        List list =  g.fromJson(obj instanceof String?(String)obj:g.toJson(obj),List.class);
        List ls = new ArrayList();
        for (Object map : list){
            try{
                ls.add(toObj(t,map));
            }catch (Exception e){
                ls.add(LX.toJSONString(map));
            }
        }
        return ls;
    }

    private static Gson g = new GsonBuilder().disableHtmlEscaping().create();

    static String toJSONString(Object obj) {
        return obj instanceof String?(String)obj:g.toJson(obj);
    }
    /**将map中的指定字段为null设置为数字*/
    static void nullToNum(Map pd , String str){
        if (isEmpty(pd)||isEmpty(str))return;
        for (String s : str.split(",")){
            s = s.trim();
            if (isEmpty(pd.get(s))) pd.put(s,0);
        }
    }
    //说明:格式化输出json字符串
    /**{ ylx } 2019/8/12 10:04 */
    static String toFormatJson(Object json) {
        JsonObject jsonObject = new JsonParser().parse(toJSONString(json)).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonObject);
    }
    /**将map中的指定字段为null设置为字符串*/
    static void nullToString(Map pd , String str){
        if (isEmpty(pd)||isEmpty(str))return;
        for (String s : str.split(",")){
            s = s.trim();
            if (isEmpty(pd.get(s))) pd.put(s,"");
        }
    }
}


