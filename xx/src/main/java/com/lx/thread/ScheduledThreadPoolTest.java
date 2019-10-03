package com.lx.thread;//说明:

import com.sun.jmx.snmp.tasks.Task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建人:游林夕/2019/4/4 09 55
 */
public class ScheduledThreadPoolTest {
    public static void main(String [] args){
        ScheduledExecutorService seh = Executors.newScheduledThreadPool(3);
        Task at = new Task() {
            @Override
            public void run() {

            }

            @Override
            public void cancel() {

            }
        };
        seh.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("1");
            }
        },1,1, TimeUnit.SECONDS);
    }
}
