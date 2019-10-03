package com.lx.thread;//说明:

import java.util.*;
import java.util.concurrent.*;

/**
 * 创建人:游林夕/2019/3/18 09 18
 */
public class ExecutorServiceTest {
    public static void main(String [] args) throws ExecutionException, InterruptedException {
//        ExecutorService ex = Executors.newCachedThreadPool();
//        List<Future<Integer>>ls = new ArrayList<Future<Integer>>();
//        for (int i =0;i<3;i++){
//            Future<Integer> fu = ex.submit(new Callable<Integer>(){
//                public Integer call() throws Exception {
//                    System.out.println("子线程开始运行"+Thread.currentThread().getName());
//                    Thread.sleep(new Random().nextInt(3000));
//                    System.out.println("子线程运行结束"+Thread.currentThread().getName());
//                    return new Random().nextInt(100);
//                }
//            });
//            ls.add(fu);
//        }
////        ex.shutdown();
//        for (Future<Integer> i : ls){
//            try {
//                System.out.println(i.get(1500,TimeUnit.MILLISECONDS));
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("主线程结束");
        futureTask();

    }
    private static void futureTask() throws ExecutionException, InterruptedException {
        final Map map = new HashMap();
        FutureTask<Map> futureTask = new FutureTask<Map>(new Callable<Map>() {
            public Map call() throws Exception {
                map.put("dd","aa");
                Thread.sleep(2000);
                return null;
            }
        });
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(futureTask);
        System.out.println(futureTask.get());
        System.out.println(map);
    }
}
