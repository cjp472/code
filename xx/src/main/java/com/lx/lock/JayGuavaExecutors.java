package com.lx.lock;//说明:

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.*;

/**
 * 使用Guava封装的线程池
 *
 * @author hetiewei
 * @date 2016年8月15日 上午11:16:54
 *
 */
public class JayGuavaExecutors {
    public static final int DEFAULT_MAX_THREAD = 1000;
    private static ListeningExecutorService defaultCompletedExecutorService = null;
    private static final Object lock = new Object();

    public static ListeningExecutorService newCachedExecutorService(int maxThreadNumber, final String namePrefix) {
        return MoreExecutors.listeningDecorator(new ThreadPoolExecutor(0, maxThreadNumber, 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
            private final AtomicInteger poolNumber = new AtomicInteger(1);
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, namePrefix + poolNumber.getAndIncrement());
                return thread;
            }
        }));
    }
    public static ListeningExecutorService newCachedExecutorService(String namePrefix) {
        return newCachedExecutorService(DEFAULT_MAX_THREAD, namePrefix);
    }
    public static ListeningExecutorService getDefaultCompletedExecutorService() {
        if (defaultCompletedExecutorService == null) {
            synchronized (lock) {
                if (defaultCompletedExecutorService == null) {
                    defaultCompletedExecutorService = newCachedExecutorService("Completed-Callback-");
                }
            }
        }
        return defaultCompletedExecutorService;
    }

}
