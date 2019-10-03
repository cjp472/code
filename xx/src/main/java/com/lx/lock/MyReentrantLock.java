package com.lx.lock;//说明:

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 创建人:游林夕/2019/3/15 08 50 继承抽象队列同步器 实现独享锁
 */
public class MyReentrantLock extends AbstractQueuedSynchronizer implements Lock, Serializable {
        public MyReentrantLock(){}
        public MyReentrantLock(boolean fair){this.fair = fair;}
        private boolean fair = false;
        //1.是否线程为独享(当前为一个线程独享)
        @Override
        protected boolean isHeldExclusively(){
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
        //2当前线程是否可以获取锁
        public void lock(){
            acquire(1);//继续判断是否可以获取锁,不行就会将线程存入队列 --->通过钩子方法调用 tryAcquire
        }
        @Override
        protected boolean tryAcquire(int state){
            if (!(fair&&hasQueuedPredecessors())//fair为true时先判断队列是否有等待的如果有没有进入队列的线程不能抢
                    &&compareAndSetState(0,state)){//尝试使用CAS进行交换锁住 (非公平锁时,队列外的线程仍然可以抢)
                setExclusiveOwnerThread(Thread.currentThread());//成功将当前线程设置为锁线程
                return true;
            }else if (getExclusiveOwnerThread() == Thread.currentThread()){//判当前断线程是否为锁线程 <<<可重入锁>>
                setState(getState()+state);
                return true;//将新的次数累加
            }
            return false;
        }
        //3尝试解锁
        public void unlock(){
            release(1);//通过钩子方法调用 tryRelease
        }
        @Override
        public boolean tryRelease(int state){
            if (Thread.currentThread() == getExclusiveOwnerThread()){
                int c = getState();
                if (c == state || c ==0){
                    setExclusiveOwnerThread(null);
                    setState(0);
                    return true;
                }
                setState(c);//将次数进行释放
            }
            return false;
        }


    public void lockInterruptibly() throws InterruptedException {

    }

    public boolean tryLock() {
        return tryAcquire(1);
    }

    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryAcquireNanos(1, unit.toNanos(time));
    }

    // 返回一个Condition，每个condition都包含了一个condition队列
    public Condition newCondition() {
        return new ConditionObject();
    }
}
