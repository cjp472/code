package com.lx.util;//说明:

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 创建人:游林夕/2019/3/29 14 57
 */
public class LogUtil {
    private static final ThreadLocal<Map<String, Object>> tl = new ThreadLocal() {
        protected Map<String, Object> initialValue() {
            return new HashMap(4);
        }
    };
    public static  <T>T get(String key){
        return (T) tl.get().get(key);
    }
    public static void put(String key,Object obj){
        tl.get().put(key,obj);
    }
    public static void remove(){
        tl.remove();
    }

    private PrintWriter pw;
    private String fileName ;
    private SimpleDateFormat sdf;
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    //%p: 输出日志信息优先级，即DEBUG，INFO，WARN，ERROR，FATAL,
    //%d: 输出日志时间点的日期或时间，默认格式为ISO8601，也可以在其后指定格式，比如：%d{yyyy-MM-dd HH:mm:ss,SSS}，输出类似：2011-10-18 22:10:28,921
    //%c: 输出日志信息所属的类目，通常就是所在类的全名
    //%t: 输出产生该日志事件的线程名
    //%l: 输出代码中的行号
    //%m: 输出代码中指定的消息,产生的日志具体信息
    //%n: 输出一个回车换行符，Windows平台为"\r\n"，Unix平台为"\n"输出日志信息换行
    //%T: 输出线程id
    private String pattern = "%d %p [T:%T %c:%l] %m%n";
    public LogUtil(){
        String path = System.getProperty("user.dir");
        int i = path.indexOf("\\");
        if (i!=-1) path = path.substring(0,i);
        sdf = new SimpleDateFormat(LX.format("'{0}/接口日志/'yyyy/MM/yyyy-MM-dd/HH'.log'",path));
    }
    public LogUtil(String fileName){
        LX.exObj(fileName,"文件名不能为空!");
        String path = System.getProperty("user.dir");
        int i = path.indexOf("\\");
        if (i!=-1) path = path.substring(0,i);
        sdf = new SimpleDateFormat(LX.format("'{0}/接口日志/'yyyy/MM/yyyy-MM-dd/'{1}'/HH'.log'",path,fileName));
    }
    public void setPattern(String pattern){
        this.pattern = pattern;
    }
    public void before(Object msg){
        printP("==>start");
        printP(msg instanceof String?(String)msg:LX.toJSONString(msg));
    }
    public void after(){
        printP("<==end");
    }
    public void info(Object msg){
        printP(msg instanceof String?(String)msg:LX.toJSONString(msg));
    }
    public void error(Exception msg){
        printE(msg);
    }
    void printP(String msg){
        if (LX.isEmpty(msg)||LX.isEmpty(pattern))return;
        String s = pattern;
        StackTraceElement[] se = Thread.currentThread().getStackTrace();
        put("MDC",get("MDC")==null?UUID.randomUUID().toString():get("MDC"));
        s=s.replace("%p",LX.left(se[2].getMethodName().toUpperCase(),6))
                .replace("%d",sdf1.format(new Date()))
                .replace("%c",se[3].getClassName())
                .replace("%l",LX.str(se[3].getLineNumber()))
                .replace("%t",Thread.currentThread().getName())
                .replace("%T",(String)get("MDC"))
                .replace("%n", "\r\n")
                .replace("%m",msg);
        print(s);
    }
    public void printE(Exception e) {
        if (LX.isEmpty(e)||LX.isEmpty(pattern))return;
        String s = pattern;
        StackTraceElement[] se = Thread.currentThread().getStackTrace();
        put("MDC",get("MDC")==null?UUID.randomUUID().toString():get("MDC"));
        s=s.replace("%p",LX.left(se[2].getMethodName().toUpperCase(),6))
                .replace("%d",sdf1.format(new Date()))
                .replace("%c", se[3].getClassName())
                .replace("%l",LX.str(se[3].getLineNumber()))
                .replace("%T",(String)get("MDC"))
                .replace("%n","\r\n")
                .replace("%m",e.getCause()!=null?e.getCause().getMessage():e.toString());
        print(s);
        print(e);
    }
    private void print(Object msg){
        String file = sdf.format(new Date());
        try{
            if (fileName != file) pw = getPrintWriter((fileName=file));
        }catch (Exception e){
            if(pw!=null) pw.close();
            return;
        }
        if (msg instanceof Exception){
            ((Exception)msg).printStackTrace(pw);
        }else{
            pw.print(msg);
            System.out.print(msg);
        }
        pw.flush();
    }

    /**获取输出流*/
    private static PrintWriter getPrintWriter(String fileName) throws Exception {
        File file = new File(fileName);
        File fileParent = file.getParentFile();
        if (fileParent != null &&!fileParent.exists())fileParent.mkdirs();
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,true), "utf-8"),true);
    }


}
