package com.lx.thread;//说明:

import com.google.common.util.concurrent.RateLimiter;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * 创建人:游林夕/2019/3/18 11 20
 */
public class SemaphoreTest {
    private static void test() throws InterruptedException {
        final RateLimiter rateLimiter = RateLimiter.create(10);
        for (int i =0 ;i<20;i++){
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("等待"+rateLimiter.acquire());
                        try {
                            Thread.sleep(new Random().nextInt(5000));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("正在执行"+Thread.currentThread().getName());
                }
            }).start();

        }

    }
    public static void main(String[] args) throws InterruptedException {
//        test();
        int N = 8;            //工人数
        Semaphore semaphore = new Semaphore(5); //机器数目
        for(int i=0;i<N;i++)
            new Worker(i,semaphore).start();
    }

    static class Worker extends Thread{
        private int num;
        private Semaphore semaphore;
        public Worker(int num,Semaphore semaphore){
            this.num = num;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println("工人"+this.num+"占用一个机器在生产...");
                Thread.sleep(2000);
                System.out.println("工人"+this.num+"释放出机器");
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
