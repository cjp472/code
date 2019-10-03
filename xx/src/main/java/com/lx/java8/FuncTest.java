package com.lx.java8;//说明:

/**
 * 创建人:游林夕/2019/3/26 17 06
 */
public class FuncTest {
    public static String test(String s){
        return s+"test";
    }
    public String test1(String s){
        return s+"te312st";
    }
    public static String get(StringFunc sf ,String in){
        return sf.test(in);
    }
    public static void main(String [] args){
        String in = "32131";
        System.out.println(get(FuncTest::test,in));
        FuncTest funcTest = new FuncTest();
        System.out.println(get(funcTest::test1,in));
    }
}
interface StringFunc{
    String test(String str);
}