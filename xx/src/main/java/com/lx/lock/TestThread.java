package com.lx.lock;//说明:

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 创建人:游林夕/2019/3/14 16 17
 */
class TestThread implements Runnable{

    private static int i;
    private static volatile int vi;
    private static AtomicInteger ai = new AtomicInteger();
    private static Integer si = 0;
    private static int ri;
    private Lock lock = new MyReentrantLock();
    private static AtomicInteger flag = new AtomicInteger();
    public void run() {
        for (int k=0;k<20000;k++){
            i++;
            vi++;
            ai.incrementAndGet();
            synchronized (si){
                si++;
            }
            lock.lock();
            try{
                ri++;
            }finally {
                lock.unlock();
            }
        }
        flag.incrementAndGet();
    }
    public static void main(String [] args) throws InterruptedException {
        TestThread t1 = new TestThread();
        TestThread t2 = new TestThread();
        ExecutorService e1 = Executors.newCachedThreadPool();
        ExecutorService e2 = Executors.newCachedThreadPool();
        e1.execute(t1);
        e2.execute(t2);
        while (true){
            if (flag.intValue() == 2){
                System.out.println("i>>>"+i);
                System.out.println("vi>>>"+vi);
                System.out.println("ai>>>"+ai);
                System.out.println("si>>>"+si);
                System.out.println("ri>>>"+ri);
                return;
            }
        }
    }
}
