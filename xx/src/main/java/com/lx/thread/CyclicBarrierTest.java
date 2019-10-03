package com.lx.thread;//说明:

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 创建人:游林夕/2019/3/18 10 37
 */
public class CyclicBarrierTest {
    public static void main(String[] args) throws InterruptedException {
        int N = 4;
//        CyclicBarrier barrier  = new CyclicBarrier(N);//所有线程执行到同一个地点 再继续后续任务

        CyclicBarrier barrier  = new CyclicBarrier(N,new Runnable() {//最后一个到达该点的线程额外执行一个任务 如果含有超时异常则不会执行
            public void run() {
                System.out.println("所有线程执行完毕"+Thread.currentThread().getName());
            }
        });
        for(int i=0;i<N;i++)
            new Writer(barrier).start();

    }
    static class Writer extends Thread{
        private CyclicBarrier cyclicBarrier;
        public Writer(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }

        @Override
        public void run() {
            System.out.println("线程"+Thread.currentThread().getName()+"正在写入数据...");
            try {
                Thread.sleep(new Random().nextInt(3000));      //以睡眠来模拟写入数据操作
                System.out.println("线程"+Thread.currentThread().getName()+"写入数据完毕，等待其他线程写入完毕");
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch(BrokenBarrierException e){
                e.printStackTrace();
            }
            try {
                Thread.sleep(new Random().nextInt(3000));      //以睡眠来模拟写入数据操作
                System.out.println("线程"+Thread.currentThread().getName()+"读取数据完毕，等待其他线程写入完毕");
                try {
                    cyclicBarrier.await(1000, TimeUnit.MILLISECONDS);//不建议使用超时机制
                } catch (TimeoutException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }catch(BrokenBarrierException e){
                e.printStackTrace();
            }
//            System.out.println("所有线程读写完毕，继续处理其他任务...");
        }
    }
}
