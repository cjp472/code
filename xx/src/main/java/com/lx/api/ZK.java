package com.lx.api;//说明:

import com.lx.util.LX;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 创建人:游林夕/2019/4/12 16 41
 */
public class ZK {
    public static void main(String [] args) throws InterruptedException {
        ZK zk = new ZK("127.0.0.1:2181");
        zk.create("/aa/bb","123");
//        new Thread(()->{
//            ZK zk = new ZK("127.0.0.1:2181");
//            while (true){
//                try {
//                    Thread.sleep(3000);
//                    System.out.println((String)zk.get("/aa/bb"));;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        Thread.currentThread().join();
    }
    private ZkClient zk;
    private Random random;
    private ConcurrentHashMap<String,Object> map;
   //说明:连接zk
   /**{ ylx } 2019/4/12 16:54 */
    public ZK(String path){
        zk = new ZkClient(path,10000,10000,new SerializableSerializer());
        random = new Random();
        map = new ConcurrentHashMap<>();
    }
    //说明:永久保存一个节点
    /**{ ylx } 2019/4/12 16:55 */
    public void create(String path,Object data){
        zk.createPersistent(path,true);//可以递归创建节点
        zk.writeData(path,data);//将数据写入节点
    }

    //说明:创建一个临时节点
    /**{ ylx } 2019/4/12 17:00 */
    public void createEphemeral(String path,Object data){
        zk.createEphemeral(path,true);//可以递归创建节点
        zk.writeData(path,data);//将数据写入节点
    }

    //说明:修改指定节点
    /**{ ylx } 2019/4/12 17:04 */
    public void edit(String path , Object data){
        zk.writeData(path,data);
    }
    //说明:递归删除指定节点
    /**{ ylx } 2019/4/12 17:03 */
    public void delete(String path){
        zk.deleteRecursive(path);
    }

    //说明:查询指定节点的值
    /**{ ylx } 2019/4/12 17:05 */
    public <T> T get(String path){
        if (LX.isEmpty(path))return null;
        T obj = null;
        if (!map.containsKey(path)){
            map.put(path,zk.readData(path));
            zk.subscribeDataChanges(path,new IZkDataListener(){
                //监听修改
                public void handleDataChange(String s, Object o) throws Exception {
                    System.out.println("修改:"+s+" : "+o);
                    map.put(s,zk.readData(path));
                }
                //监听删除
                public void handleDataDeleted(String s) throws Exception {
                    map.remove(s);
                }
            });
        }
        return (T)map.get(path);
    }

    //说明:返回所有的子节点的地址
    /**{ ylx } 2019/4/12 17:07 */
    public List<String> getChildren(String path){
        return zk.getChildren(path);
    }

    //说明:随机获取一个子节点
    /**{ ylx } 2019/4/12 17:14 */
    public String getChildrenOne(final String path){
        if (LX.isEmpty(path))return null;
        List<String> ls = null;
        if (!map.containsKey(path)){
            zk.subscribeChildChanges(path, new IZkChildListener() {
                @Override
                public void handleChildChange(String s, List<String> list) throws Exception {
                    System.out.println("修改:"+s);
                    map.put(path,getChildren(path));
                }
            });
            map.put(path,getChildren(path));
        }else{
            ls = (List<String>)map.get(path);
        }
        if (LX.isEmpty(ls)) return null;
        return ls.get(random.nextInt(ls.size()));
    }

    //说明:关闭zk
    /**{ ylx } 2019/4/12 16:53 */
    public void close(){
        if (zk != null) zk.close();
    }

    public ZkClient getZk(){return zk;}
}
