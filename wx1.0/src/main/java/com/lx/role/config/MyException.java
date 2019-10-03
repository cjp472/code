package com.lx.role.config;//说明:

import com.lx.util.LX;

/**
 * 创建人:游林夕/2019/5/25 10 13
 */
public class MyException extends RuntimeException{
    public MyException(String msg){
        super(msg);
    }
    public static void exEmpty(Object obj,String msg){
        if (LX.isEmpty(obj)) throw new MyException(msg);
    }
}
