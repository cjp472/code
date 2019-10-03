package com.lx.超时订单;//说明:

/**
 * 创建人:游林夕/2019/5/15 14 49
 */
public class Test {
    public static void main(String [] args){
        String cron = "* * 31,3,2 * *";
        String [] arr = cron.split(" ");
        System.out.println(arr[2].matches("(\\*)|(([1-2]?[0-9]|3[0-1])(/([1-2]?[0-9]|3[0-1]))?)|(([1-2]?[0-9]|3[0-1])(\\-([1-2]?[0-9]|3[0-1]))?)|(([1-2]?[0-9]|3[0-1])(,([1-2]?[0-9]|3[0-1]))*)"));
    }
}
