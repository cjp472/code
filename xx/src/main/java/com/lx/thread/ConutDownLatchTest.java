package com.lx.thread;//说明:

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 创建人:游林夕/2019/3/14 16 03
 */
public class ConutDownLatchTest {
    public static void main(String [] args) throws InterruptedException {
        int i = 3;//定义线程数量
        final CountDownLatch latch = new CountDownLatch(i);
        for (;i>0;i--){
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println(Thread.currentThread().getName()+"开始工作");
                        Thread.sleep(new Random().nextInt(1000)+1000);
                        System.out.println(Thread.currentThread().getName()+"完成工作");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                }
            },"线程"+i).start();

        }
        System.out.println("主线程等待执行");
        latch.await();
        System.out.println("主线程开始执行");
    }
}
