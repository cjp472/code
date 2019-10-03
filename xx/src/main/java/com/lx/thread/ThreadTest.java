package com.lx.thread;//说明:

import com.lx.lock.MyReentrantLock;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * 创建人:游林夕/2019/3/19 08 53
 */
public class ThreadTest {
    static class ThreadLocalUtil{//threadlocal工具类
        static ThreadLocal<String> threadLocal = new ThreadLocal<String>();//创建对象
        public static String get(){return threadLocal.get();}//获取当前线程的值
        public static void set(String val){threadLocal.set(val);}//设置当前线程的值
    }
    /**volatile变量，确保对一个变量的更新以可预见的方式告知其它的线程。对它的操作不会与其它的内存操作一起被重排序。
　　volatile变量的操作不会加锁，也不会引起执行线程的阻塞，这使得volatile变量相对于sychronized而言是一种轻量级的同步机制。
　　volatile变量通常被当做是标识完成、中断、状态的标记使用。它也存在一些限制。volatile变量只能保证可见性，而加锁可以保证可见性和原子性。
　　只有满足下面所有的标准后，你才能使用volatile变量：
     写入变量时并不依赖变量的当前值，或者能够确保只有单一的线程修改变量的值
     变量不需要与其它的状态量共同参与不变约束
     访问变量时，没有其他的原因需要加锁

　　ThreadLocal是当前线程私有的一个Map 已自己为key 有get和set方法 不存在多线程问题!
    */
     public static void main(String [] args) throws InterruptedException {
         Object obj = new Object();
         ThreadLocalUtil.set("main-threadLocal");
         System.out.println(Thread.currentThread().getName()+":我设置了ThreadLocal:"+ ThreadLocalUtil.get());
         /*1.NEW 新建状态*/
        Thread t1 = new T1(obj,"杰克");//创建继承Thread类的线程
        Thread t2 = new Thread(new T2(obj),"肉丝");//创建实现Runnable接口的线程
        /*2 RUNNABLE 可运行状态 -- RUNNING 运行状态  通过进程调度获取时间片的线程进入运行状态 时间片结束或调用yield()方法*/
        t1.setPriority(Thread.MIN_PRIORITY);//设置线程优先级
        t2.setPriority(Thread.MAX_PRIORITY);
        t1.start();//
        t2.start();
        ThreadLocal<String> ss = new ThreadLocal<String>();
        System.out.println(Thread.currentThread().getName()+":action!");
        t2.join();//当前线程等待指定线程结束后再执行
        System.out.println(Thread.currentThread().getName()+":jump ok!");
        System.out.println(Thread.currentThread().getName()+":我的还是:"+ThreadLocalUtil.get());
        Thread.sleep(3000);//使线程进入阻塞,不会释放锁

        //通知线程t1终止  不要以为它是中断某个线程！
        // 它只是线线程发送一个中断信号，让线程在无限等待时（如死锁时）能抛出抛出，从而结束线程，
        // 但是如果你吃掉了这个异常，那么这个线程还是不会中断的！
        //对某一线程调用 interrupt()时，如果该线程正在执行普通的代码，那么该线程根本就不会抛出InterruptedException。
        // 但是，一旦该线程进入到 wait()/sleep()/join()后，就会立刻抛出InterruptedException 。
        t1.interrupt();
        System.out.println(Thread.currentThread().getName()+":stop!");
        /*3 BLOCKED*/
        //3.1 等待阻塞 -- 执行wait方法 JVM会将线程放入等待池 主动放弃时间片 并释放锁 等待notify 或 notifyAll 方法进入锁池竞争锁 wait和notify方法必须在synchronized内
        //3.2 同步阻塞 -- 线程在等待获取锁的时候会阻塞
        //3.3 其他阻塞 -- 线程在调用sleep()或jion()方法时会进入超时等待队列
    }



}
/**1.1创建线程有两种方式:继承Thread*/
class T1 extends Thread{
    private Object obj;
    public T1(Object obj,String name){super(name);this.obj = obj;}
    public void run(){
//        synchronized (obj){
            try {
                ThreadTest.ThreadLocalUtil.set("T1-threadLocal");
                System.out.println(Thread.currentThread().getName()+":我将ThreadLocal修改了"+ ThreadTest.ThreadLocalUtil.get());
                //obj.wait();//主动放弃时间片 并释放锁
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName()+":肉丝");
                Thread.yield();//主动让出时间片 进入可运行状态,但随时都有可能重新执行 不会释放锁
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        }

        while(true){
            try {
                Thread.sleep(2000);//使线程进入阻塞,不会释放锁
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new Error(Thread.currentThread().getName()+":我被人踢下去了!");
            }
            System.out.println(Thread.currentThread().getName()+":you jump i jump!");
        }
    }
}
/**1.2实现Runnable接口   优点:1.类可以实现多个接口,只能继承一个类 2.线程池只能放入Runnable或Callable的子类 */
class T2 implements Runnable{
    private Object obj;
    public T2(Object obj){this.obj = obj;}
    public void run() {
        System.out.println(Thread.currentThread().getName()+":杰克");
//        synchronized (obj){
//            obj.notify();//通知当前对象的所有wait对象从等待队列进入锁池竞争锁
//        }
        for (int i=0;i<3;i++){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+":i want jump!");
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+":i jump!");
    }
}
