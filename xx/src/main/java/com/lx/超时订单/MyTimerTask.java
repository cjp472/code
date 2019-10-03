package com.lx.超时订单;//说明:

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 创建人:游林夕/2019/5/15 10 59
 */
public class MyTimerTask extends TimerTask {



    public static void main(String [] args){
        MyTimerTask m = new MyTimerTask();
        for (int i = 0;i<100;i++){
            m.add(new Task<Runnable>(1000,new T(i*1000)));
        }
    }
    @Override
    protected List<Task<Runnable>> init() {
        return null;
    }

    @Override
    protected void complete(Task<Runnable> task) {
        System.out.println("任务完成:"+task);

    }

    @Override
    protected void exception(Task<Runnable> task) {
        System.out.println("任务异常:"+task);
    }

    static class T implements Runnable{
        int time;
        public T(int time){
            this.time = time;
        }
        @Override
        public void run() {
            System.out.println("我在执行任务"+time);
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            int i = 1/0;
            System.out.println("执行完毕!"+time);
        }
    }


}
