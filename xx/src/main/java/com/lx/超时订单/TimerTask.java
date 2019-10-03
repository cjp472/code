package com.lx.超时订单;//说明:

import java.util.List;
import java.util.concurrent.*;

/**
 * 创建人:游林夕/2019/5/15 09 28
 */
public abstract class TimerTask{
    private final DelayQueue<Task<Runnable>> queue;
    private final ExecutorService es;
    public TimerTask(){
        queue = new DelayQueue();
        es = Executors.newCachedThreadPool();
        List<Task<Runnable>> ls = init();//获取未执行的任务
        if (ls != null){//将初始任务加入队列
            for (Task<Runnable> task : ls){
                add(task);
            }
        }
        run();//开始执行任务
    }
    //说明:启动时检查数据库中过期未支付订单
    //将未过期任务加入delayQueue中 过期时间 - 当前时间计算过期时间
    /**{ ylx } 2019/5/15 9:55 */
    protected abstract List<Task<Runnable>> init();

    //说明:任务完成时处理方案
    /**{ ylx } 2019/5/15 10:46 */
    protected abstract void complete(Task<Runnable> task);
    //说明:发生异常时调用
    /**{ ylx } 2019/5/15 11:02 */
    protected abstract void exception(Task<Runnable> task);
    //说明:添加任务
    /**{ ylx } 2019/5/15 9:49 */
    public void add(Task<Runnable> r){
        queue.add(r);
    }
    //说明:运行任务
    /**{ ylx } 2019/5/15 10:06 */
    protected void run(){
        es.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Task<Runnable> task = queue.take();//获取任务
                        Future f = es.submit(task.getT());//执行任务并等待成功
                        es.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    f.get();
                                    complete(task);//任务完成执行
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {//任务异常
                                    exception(task);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    static class Task<T> implements Delayed {
        private long time;
        private T t;
        private Object data;
        class Scheduled{
            private  String cron = "* * *";
            public Scheduled(String cron){
                if (cron == null) throw new RuntimeException("不能将定时器的参数设置为空!");
                String [] arr = cron.split(" ");
                if (arr.length != 3
                        //毫秒允许的值为:* 或者3位数{/3位数} 或者3位数{,3位数}
//                        || !arr[0].matches("(\\*)|(\\d{1,3}(/\\d{1,3})?)|(\\d{1,3}(\\-\\d{1,3})?)|(\\d{1,3}(,\\d{1,3})*)")
                        //秒允许的值为:* 或者0-59{/0-59} 或者0-59{-0-59} 或者0-59{,0-59}
                        || !arr[1].matches("(\\*)|([1-5]?[0-9](/[1-5]?[0-9])?)|([1-5]?[0-9](\\-[1-5]?[0-9])?)|([1-5]?[0-9](,[1-5]?[0-9])*)")
                        //分允许的值为:* 或者0-59{/0-59} 或者0-59{-0-59} 或者0-59{,0-59}
                        || !arr[2].matches("(\\*)|([1-5]?[0-9](/[1-5]?[0-9])?)|([1-5]?[0-9](\\-[1-5]?[0-9])?)|([1-5]?[0-9](,[1-5]?[0-9])*)")
                        //时允许的值为:* 或者0-24{/0-24} 或者0-24{-0-24} 或者0-24{,0-24}
                        || !arr[3].matches("(\\*)|((1?[0-9]|2[0-3])(/(1?[0-9]|2[0-3]))?)|((1?[0-9]|2[0-3])(\\-(1?[0-9]|2[0-3]))?)|((1?[0-9]|2[0-3])(,(1?[0-9]|2[0-3]))*)")
                        //天允许的值为:* 或者0-24{/0-24} 或者0-24{-0-24} 或者0-24{,0-24}
//                        || !arr[4].matches("(\\*)|(([1-2]?[0-9]|3[0-1])(/([1-2]?[0-9]|3[0-1]))?)|(([1-2]?[0-9]|3[0-1])(\\-([1-2]?[0-9]|3[0-1]))?)|(([1-2]?[0-9]|3[0-1])(,([1-2]?[0-9]|3[0-1]))*)")
                )
                this.cron = cron;
            }
            public long getNextTime(){
                String [] arr = cron.split(" ");

                return 0;
            }
            private long getNextTime(String s , int pow){
                if (s.matches("(\\*)|(0)")){
                    return 0;
                }else if (s.matches("\\d+")){
                    return Integer.parseInt(s)*pow;
                }else if (s.matches("")){

                }
                return 1;
            }

        }
        public Task(long time,T t){
            this(time,t,null);
        }
        public Task(long time,T t,Object data){
            this.data = data;
            this.time = time;
            this.t = t;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.time - System.currentTimeMillis(),unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS))<0?-1:1;
        }

        public long getTime() {
            return time;
        }

        public Object getData() {
            return data;
        }

        public T getT() {
            return t;
        }
        @Override
        public String toString() {
            return "Task{" +
                    "time=" + time +
                    ", data=" + data +
                    ", t=" + t +
                    '}';
        }
    }
}

