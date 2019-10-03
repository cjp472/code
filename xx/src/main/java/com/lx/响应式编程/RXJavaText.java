package com.lx.响应式编程;//说明:

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import sun.rmi.runtime.Log;

import java.io.Serializable;

import static java.sql.DriverManager.println;

/**
 * 创建人:游林夕/2019/3/25 17 21
 */
public class RXJavaText {
    private static void error() throws Exception {throw new Exception("出现错误!");}
    public static int getInt(){return 1;}
    public static String getString(){return "wsl";}
    public static boolean getBoolean(){return true;}
    public static void main(String [] args){




        //创建上游 Observable（被观察者）
        Observable.create(new ObservableOnSubscribe<Integer>() {//创建被观察者
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                error();
                emitter.onNext(3);
                emitter.onComplete();
            }
//        }).subscribe( new Consumer<Integer>() {//绑定观察者只关心onNext
//            public void accept(Integer value) throws Exception {
//                System.out.println("onNext:"+value);
//            }
//        });

        }).subscribe( new Observer<Integer>() {//所有的都观察
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println(d);
            }

            @Override
            public void onNext(Integer value) {//下一步
                System.out.println("onNext:"+value);
            }

            @Override
            public void onError(Throwable e) {//异常
                System.out.println("error"+e);
            }

            @Override
            public void onComplete() {//完成
                System.out.println("onComplete");
            }
        });


    }
}
