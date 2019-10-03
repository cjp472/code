package com.lx.超时订单;//说明:

import com.lx.thread.ThreadTest;

import java.util.Arrays;
import java.util.Random;

/**
 * 创建人:游林夕/2019/3/19 15 40
 */
public class MyBlockQueue<T>{
    public static void main(String [] args) throws InterruptedException {
        final MyBlockQueue<String> queue = new MyBlockQueue<String>();
        for (int i=0;i<2;i++) {
            new Thread(new Runnable() {
                public void run() {
                    Random random = new Random();
                    while (true) {
                        int i = random.nextInt(1000) + 1000;
                        try {
                            queue.put("" + i);
                            Thread.sleep(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, "插入线程"+i).start();
        }
        for (int i=0;i<4;i++){
            new Thread(new Runnable() {
                public void run() {
                    Random random = new Random();
                    while (true){
                        int i = random.nextInt(2000)+2000;
                        try {
                            queue.task();
                            Thread.sleep(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            },"取出线程"+i).start();
        }


    }
    T [] arr = (T[])new Object[10];
    Object o1 = new Object();
    Object o2 = new Object();
    int count = 0;
    public  void put(T t) throws InterruptedException {
        synchronized (arr) {
            System.out.println(Thread.currentThread().getName() + "等待存入:" + t);
            while(count >= arr.length){
                synchronized (o1){
                    System.out.println("队列已满");
                    o1.wait();
                }
            }

            count++;
            arr[arr.length - count] = t;
            synchronized (o2){
                o2.notifyAll();
            }
            System.out.println(Thread.currentThread().getName() + "存入成功!");
        }


    }
    public  T task() throws InterruptedException {
        T t = null;
        System.out.println(Thread.currentThread().getName() + "等待取出!");
        while (count <= 1){
            synchronized (o2){
                System.out.println("队列已空");
                o2.wait();
            }
        }

        t = arr[arr.length - 1];
        System.arraycopy(arr, 0, arr, 1, arr.length - 1);
        count--;
        synchronized (o1){
            o1.notifyAll();
        }
        System.out.println(Thread.currentThread().getName() + "取出成功!" + t + "--" + Arrays.toString(arr));
        return t;
    }
}
