package com.lx.lock;//说明:

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/**
 * 创建人:游林夕/2019/3/21 09 38
 * 可重入读写锁
 */
public class MyReentrantReadWriteLock implements ReadWriteLock {
    static class MyThread implements Runnable{
        private boolean r;
        private Lock read,write;
        public MyThread(boolean r,Lock read,Lock write){
            this.read = read;
            this.r = r;
            this.write = write;
        }

        @Override
        public void run() {
            try {
                if (r){
                    read.lock();
                    System.out.println(Thread.currentThread().getName()+"开始读取数据");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName()+"读取数据完毕");
                    read.unlock();
                }else{
                    write.lock();
                    System.out.println(Thread.currentThread().getName()+"开始写入数据");
                    Thread.sleep(1000);
                    read.lock();
                    System.out.println(Thread.currentThread().getName()+"开始读取数据");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName()+"读取数据完毕");
                    read.unlock();
                    System.out.println(Thread.currentThread().getName()+"写入数据完毕");
                    write.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String [] args){
        ReadWriteLock rwl = new MyReentrantReadWriteLock(false);
        Lock read = rwl.readLock();
        Lock witer = rwl.writeLock();
        Random ra = new Random();
//        for (int i =0;i<1000;i++){
//            boolean b = ra.nextBoolean();
//            new Thread(new MyThread(b,read,witer)
//                    ,"线程"+(b?"读":"写")+i).start();
//        }
        boolean b = true;
        boolean b1 = false;
        new Thread(new MyThread(b,read,witer)
                ,"线程"+(b?"读":"写")+1).start();
        new Thread(new MyThread(b1,read,witer)
                ,"线程"+(b1?"读":"写")+3).start();
        new Thread(new MyThread(b,read,witer)
                ,"线程"+(b?"读":"写")+2).start();
        new Thread(new MyThread(b1,read,witer)
                ,"线程"+(b1?"读":"写")+4).start();
        new Thread(new MyThread(b,read,witer)
                ,"线程"+(b?"读":"写")+5).start();
        new Thread(new MyThread(b,read,witer)
                ,"线程"+(b?"读":"写")+6).start();
        new Thread(new MyThread(b,read,witer)
                ,"线程"+(b?"读":"写")+7).start();

    }
    public MyReentrantReadWriteLock(boolean fair){
        this.aqs = new Aqs(fair);
    }
    class ThreadLocalUtil<Integer> extends ThreadLocal<java.lang.Integer> {
        @Override
        protected java.lang.Integer initialValue() {
            return 0;
        }
    }
    class Aqs extends AbstractQueuedSynchronizer{
        boolean fair = false;//是否公平
        boolean witer = false;//是否为写锁
        public Aqs(boolean fair){this.fair = fair;}
        ThreadLocalUtil<Integer> tlu = new ThreadLocalUtil<Integer>();

        @Override
        protected boolean tryAcquire(int arg) {
            if (fair&&hasQueuedPredecessors())
                return false;
            if (compareAndSetState(0,1)){     //公平锁先判断队列是否有值//进行cas操作
                this.witer = arg == 1;//写锁
                tlu.set(tlu.get()+1);
                setExclusiveOwnerThread(Thread.currentThread());        //设置为当前线程
                return true;
            }else if(arg == 1&&Thread.currentThread() == getExclusiveOwnerThread()){
                throw new RuntimeException("同一个线程不能在获取读锁的情况下获取写锁!");
            } else if(arg==0||(witer&&Thread.currentThread() == getExclusiveOwnerThread())){//可重入 arg==0表示读锁
                int state = getState();
                int next = state+1;
                while(true){
                    if (compareAndSetState(state,next)){
                        tlu.set(tlu.get()+1);
                        return true;
                    }
                    state = getState();
                    next = state+1;
                }

            }
            return false;
        }
        @Override
        protected boolean tryRelease(int arg) {
            if (tlu.get()>0){
                //当前线程进入过读锁
                tlu.set(tlu.get()-1);//将当前线程减1
            }else if(Thread.currentThread() != getExclusiveOwnerThread() || tlu.get() == 0)
                throw new IllegalMonitorStateException();
            boolean free = false;
            int state = getState();
            int next = state-1;
            while(true){
                if(compareAndSetState(state,next)){
                    if (next == 0) {
                        free = true;
                        setExclusiveOwnerThread(null);
                    }
                    System.out.println(next);
                    return free;
                }
                state = getState();
                next = state-1;
            }



        }

        public Condition getCondition(){
            return new ConditionObject();
        }
    }
    private Aqs aqs;
    class  MyLock implements Lock {
        public void lock(){
            aqs.acquire(1);
        }
        public void unlock() {
            aqs.release(1);
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
            return aqs.getCondition();
        }
    }
    //读锁 共享锁
    public Lock readLock() {
        return new MyLock() {
            public void lock(){
                aqs.acquire(0);
            }
        };
    }

    //写锁 独占锁
    public Lock writeLock() {
        return new MyLock(){};
    }


}
