package com.lx.lock;//说明:

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 创建人:游林夕/2019/3/18 14 26
 */
public class MapTest {
    public static void main(String [] args) throws InterruptedException {
        final Map<String,Integer> map = new MyMap<String, Integer>();
        int N = 10000;
        final CountDownLatch latch = new CountDownLatch(N);
        final Lock lock = new ReentrantLock();
        map.put("aa",0);
        for (int i=0;i<N;i++){
            new Thread(new Runnable() {
                public void run() {
//                    lock.lock();
                    try{
                        map.put("aa",map.get("aa")+1);
                    }finally{
//                        lock.unlock();
                        latch.countDown();
                    }


                }
            }).start();
        }
        latch.await();
        System.out.println(map.get("aa"));

    }
}

class MyMap<K,V> extends HashMap<K,V>{
    Lock lock = new ReentrantLock();
    @Override
    public V get(Object key) {
        lock.lock();
        try {
            return super.get(key);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
        return super.put(key, value);
        }finally {
            lock.unlock();
        }
    }
}