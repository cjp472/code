package com.lx.thread;//说明:

import com.lx.util.LX;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建人:游林夕/2019/4/1 16 20
 */
public class MyExecutor implements Executor {
    private int max,min;
    private LinkedBlockingQueue<Runnable> lbq;
    private AtomicInteger ac = new AtomicInteger();
    public MyExecutor(int i){
        this.lbq = new LinkedBlockingQueue<>();
        this.max = i;
        for (int j=0;j<i;j++){
            new Thread(()->{
                try {
                    Runnable r;
                    while ((r=lbq.take())!=null)
                        r.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void execute(Runnable command) {
        LX.exObj(command,"Runnable不能为空!");
        try {
            lbq.put(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
