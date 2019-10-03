package com.lx.lock;//说明:



import com.google.common.util.concurrent.*;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 创建人:游林夕/2019/3/11 20 39
 */
public class CasTest {
    final static ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    static int i = 1000;
    public static void main(String [] args) throws InterruptedException {
        int count = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(count);
        final Lock lock = new MyReentrantLock(true);
        long time1 = System.currentTimeMillis();
        String s;
        for (int j = 0;j<count;j++){
            final ListenableFuture<Integer> booleanTask = service.submit(new Callable<Integer>() {
                public Integer call() throws Exception {
                    while (true) {
                        System.out.println(Thread.currentThread().getName()+":"+i);
                        if (i-1<0)break;
                        lock.lock();
//                        synchronized (lock){
                            i--;
//                        }
                        lock.unlock();
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return i;
                }
            });
            Futures.addCallback(booleanTask, new FutureCallback<Integer>() {
                public void onSuccess(Integer result) {
                    System.err.println("BooleanTask: " + result);
                    countDownLatch.countDown();
                }
                public void onFailure(Throwable throwable) {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
//        service.shutdown();
        // 执行时间
        System.err.println("time: " + (System.currentTimeMillis() - time1));
    }
}


