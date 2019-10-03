package com.lx.Async;//说明:

import java.util.concurrent.*;

/**
 * 创建人:游林夕/2019/3/26 09 18
 */
public class FutureAndCallable {
    public static void main(String [] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();
        Future<Integer> future = es.submit(()->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 2;
        });
        Future<Integer> future1 = es.submit(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 1;
        });
        System.out.println(future.get());
        System.out.println(future1.get());
        es.shutdown();


    }
}
