package com.lx.thread;//说明:

import com.lx.util.LX;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 创建人:游林夕/2019/3/19 16 09
 */
public class Test {
    static int a,b,x,y=0;
    public static void main(String [] args) throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                    a = 1;
                    x = b;
                System.out.println("结束");
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                b = 1;
                y = a;
                System.out.println("结束");
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("x:"+x);
        System.out.println("y:"+y);
    }
}
