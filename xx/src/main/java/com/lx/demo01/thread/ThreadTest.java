package com.lx.demo01.thread;//说明:

/**
 * 创建人:游林夕/2019/5/13 16 36
 */
public class ThreadTest {
    public static void main(String [] args) throws InterruptedException {
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();
        new Thread(new T(o1,o2),"A").start();
        Thread.sleep(10);
        new Thread(new T(o2,o3),"B").start();
        Thread.sleep(10);
        new Thread(new T(o3,o1),"C").start();
        Thread.sleep(10);
    }
}
class T implements Runnable{
    Object self,next;
    public T(Object self,Object next){
        this.self = self;
        this.next = next;
    }
    @Override
    public void run() {
        for (int i =0;i<10;i++){
            synchronized (self){
                synchronized (next){
                    System.out.print(Thread.currentThread().getName()+i +"\t");
                    next.notify();
                }
                try {
                    self.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}