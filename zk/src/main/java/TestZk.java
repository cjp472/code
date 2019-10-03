import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.List;

/**
 * 创建人:游林夕/2019/4/11 09 10
 */
public class TestZk {
    static final ZkClient zc = new ZkClient("220.194.101.89:2181",10000,10000,new SerializableSerializer());
    static final String DB_PATH="/TXApp";
    public static void main(String [] args) throws Exception {
        //对父节点添加监听子节点变化。
        zc.subscribeChildChanges("/super", new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                System.out.println("parentPath: " + parentPath);
                System.out.println("currentChilds: " + currentChilds);
            }
        });
        Thread.sleep(3000);

        zc.createPersistent("/super");
        Thread.sleep(1000);

        zc.createPersistent("/super" + "/" + "c1", "c1内容");
        Thread.sleep(1000);

        zc.createPersistent("/super" + "/" + "c2", "c2内容");
        Thread.sleep(1000);

        zc.delete("/super/c2");
        Thread.sleep(1000);

        zc.deleteRecursive("/super");
        Thread.sleep(Integer.MAX_VALUE);
    }

    public void find(){
        System.out.println(zc.getChildren("/TXApp/config"));;
    }
    public void add() throws Exception {
//        create(DB_PATH,"",true);
        create("/TXApp/config/appUrl", "https://m.ttxinyi.com/TXApp/app/sync/service?METHOD=",true);

//        create("/TXApp/config/hisMethod", "getRegisteredNum,saveRegisterIsOnline,savePayOrder,getPrescriptionMasterList,confirmPrescriptionYB,getPatientRegisterStateCountS,getPrescriptions,getPrescriptionBasic,getPrescriptionDetail,findDrug,containsDrug,updatePayOrder,tradeRefundSuccess,confirmRecByOnline,",true);
//        create("/TXApp/config/appMethod", "returnAllPay,returnFeeByOffline,selectPayOrderAndPayment,getPayStr,saveReceiptDetail,saveRegisterInfoByTime,offlineSetPushMsg,saveAllMTDept,saveOutpVisitReals,saveAllDoctor,saveAllDocLinkDept,saveAllDept,findPaymentByDate,tradeRefundByOffline,saveAllYBDept,offlineSetPushMsg,",true);
//        System.out.println((String)zc.readData(DB_PATH));
//        System.out.println((InetAddress)zc.readData("/appUrl"));
//        System.in.read();
    }
//    @Test
    public void edit(){
        update(DB_PATH,"127.0.0.4");
    }
    public void delete(){
        zc.deleteRecursive("/appUrl");
    }

    private void create(String path,Object obj,boolean persistent){
        if (persistent){
            zc.createPersistent(path,true);//递归创建节点
        }else{
            zc.createEphemeral(path,true);
        }
        update(path,obj);//写入文件
    }

    public void update(String path,Object data){
        zc.writeData(path,data);
    }
}


class ZKThread implements Runnable{

    @Override
    public void run() {
        final ZkClient zc = TestZk.zc;
        String s = zc.readData(TestZk.DB_PATH);
        System.out.println(Thread.currentThread().getName()+":"+s);
        //监听DB_PATH节点是否被修改
        zc.subscribeDataChanges(TestZk.DB_PATH, new IZkDataListener() {
            @Override//地址被修改
            public void handleDataChange(String s, Object o) throws Exception {
                System.out.println("地址被修改为:"+o);
            }

            @Override//地址被删除
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("地址被删除");
            }
        });
        System.out.println("3123");
    }
}