package com.lx.lock;//说明:


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

/**
 * 创建人:游林夕/2019/3/14 11 20
 */
public class LXLock implements Lock {
    public boolean fair;
    volatile int i = 0;
    volatile AtomicReference<Thread> atomicReference = new AtomicReference<Thread>();
    volatile LinkedBlockingQueue<Thread> linkedBlockingQueue = new LinkedBlockingQueue<Thread>();


    public LXLock(){}
    //为true时使用同步锁
    public LXLock(boolean fair){this.fair = fair;}

    public void lock() {
        while((fair?linkedBlockingQueue.size()==0:true)
                &&!atomicReference.compareAndSet(null,Thread.currentThread())){
            Thread thread = Thread.currentThread();
            if (atomicReference.get()==thread){
                i++;
                return;
            }
            linkedBlockingQueue.add(thread);
            LockSupport.park();
            linkedBlockingQueue.remove(thread);
        }
        i++;
    }

    public void unlock() {
        if (atomicReference.get()==Thread.currentThread()&&--i==0&&atomicReference.compareAndSet(Thread.currentThread(),null)){
            for (Thread t : linkedBlockingQueue){
                LockSupport.unpark(t);
            }
        }
    }

    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return false;
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }


    public Condition newCondition() {
        return null;
    }
}
