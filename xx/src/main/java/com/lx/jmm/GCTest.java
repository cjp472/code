package com.lx.jmm;//说明:

/**
 * 创建人:游林夕/2019/4/4 15 15
 */
public class GCTest {
    private static final int _1MB = 1024*1024;

    public static void testAllocation(){

        byte[] allocation1 , allocation2 , allocation3 , allocation4;

        allocation1 = new byte[2 * _1MB];
        allocation2 = new byte[2 * _1MB];
        allocation3 = new byte[2 * _1MB];
        allocation4 = new byte[6 * _1MB];
        String str = "1231";
        while(true){
            str+=str;
        }
    }

    public static void main(String[] args) {
        GCTest.testAllocation();
    }
}
