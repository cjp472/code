package com.lx.超时订单;

import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2019/3/10.
 */
public class TestBL {
    public static void main(String[] args) {
        final DelayQueue<Order> delayQueue = new DelayQueue<Order>();
        //启动时检查数据库中过期未支付订单
        // 将未过期订单加入delayQueue中 过期时间 - 当前时间计算过期时间

        new Thread(new Runnable() {
            Random r = new Random();
            public void run() {
                while (true){
                    long l = r.nextInt(3000)+1000;
                    Order<Object> o = new Order<Object>(l,"订单"+l);
                    System.out.println("加入线程"+o);
                    delayQueue.add(o);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
      Thread t =  new Thread( new Runnable(){
            public void run() {
                Order<Object> obj = null;
                try {
                    while(true){
                        obj = delayQueue.take();
                        if (null != obj){
                            //检查订单;
                            System.out.println(obj);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
       System.out.println("订单线程启动成功!");
        // t.interrupt();//关闭线程

    }
}
class Order<T> implements Delayed{
    private long time;
    private T data;

    @Override
    public String toString() {
        return "Order{time=" + time +", data=" + data +"}";
    }

    public long getTime(){return time;}
    public Order(long time,T data){
        super();
        this.time = time + System.currentTimeMillis();
        this.data = data;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.currentTimeMillis(),unit);
    }

    public int compareTo(Delayed o) {
        long d = (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        if (d == 0){
            return 0;
        }else{
            if (d<0){
                return -1;
            }else{
                return 1;
            }
        }
    }
}
