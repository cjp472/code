package com.lx.home.service.jea;

import com.lx.authority.config.OS;
import com.lx.entity.Var;
import com.lx.home.dao.Dao;
import com.lx.home.dao.DaoSupport;
import com.lx.util.LX;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 游林夕 on 2019/9/19.
 */
@Service
public class ShipmentService {

    public OS.Page list(Map map){
        if(LX.isNotEmpty(map.get("start_end"))){
            String [] arr = map.get("start_end").toString().split(" - ");
            if (arr.length != 2) LX.exMsg("时间出现问题!");
            map.put("s_time",arr[0].trim());
            map.put("e_time",arr[1].trim());
        }
        return DaoSupport.dao.listPage("ShipmentMapper.list",map);
    }
    //说明:复制一条
    /**{ ylx } 2019/9/29 18:54 */
    public void copy_shipment(Map map){
        LX.exMap(map,"id");
        int exec = DaoSupport.dao.exec("ShipmentMapper.copy_shipment", map);
        if (exec<1){
            LX.exMsg("复制失败!");
        }
    }
    //说明:退货
    /**{ ylx } 2019/9/28 20:28 */
    public void return_goods(Map map){
        LX.exMap(map,"id");
        Map m = DaoSupport.dao.findforObj("ShipmentMapper.return_goods", map);
        if(LX.isNotEmptyMap(m,"msg")){
            LX.exMsg((String) m.get("msg"));
        }
    }
    //说明:换货
    /**{ ylx } 2019/9/28 20:26 */
    public void exchange_goods(Map map){
        LX.exMap(map,"id");
        Map m = DaoSupport.dao.findforObj("ShipmentMapper.exchange_goods", map);
        if(LX.isNotEmptyMap(m,"msg")){
            LX.exMsg((String) m.get("msg"));
        }
    }
    //说明:开始发货
    /**{ ylx } 2019/9/28 19:41 */
    @Transactional
    public void deliver_goods(Map map) throws Exception {
        LX.exMap(map,"id");
        Map m = DaoSupport.dao.findforObj("ShipmentMapper.deliver_goods", map);
        if(LX.isNotEmptyMap(m,"msg")){
           LX.exMsg((String) m.get("msg"));
        }
    }
    //说明:解析CSV对账单
    /**{ ylx } 2019/7/2 15:53 */
    @Transactional()
    public void parseCSV(InputStream in) throws Exception {
        BufferedReader br = br = new BufferedReader(new InputStreamReader(in,"gbk"));;
        Map<Integer,String> map = new HashMap<>();//订单结算
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.replace("\r","").replace("\n","").replace("\t","");
            String [] arr = line.split(",");
            if (line.startsWith("订单号")){
                for (int i=0;i<arr.length;i++){
                    switch (arr[i]){
                        case "订单号":
                            map.put(i,"orderNo");
                            break;
                        case "订购数量":
                            map.put(i,"quan");
                            break;
                        case "下单时间":
                            map.put(i,"addTime");
                            break;
                        case "客户姓名":
                            map.put(i,"name");
                            break;
                        case "客户地址":
                            map.put(i,"address");
                            break;
                        case "联系电话":
                            map.put(i,"phone");
                            break;
                        case "订单备注":
                            map.put(i,"remark");
                            break;
                        case "商家备注":
                            map.put(i,"remark_sj");
                            break;
                        case "订单状态":
                            map.put(i,"order_status");
                            break;
                        case "结算金额":
                            map.put(i,"total");
                            break;
                    }
                }
            }else if (line.matches("\\d+,.*")){
                Var m = new Var();
                for (Integer i : map.keySet()){
                    m.put(map.get(i),arr[i].replace("=","").replace("\"",""));
                }
                m.put("lx_orderNo",m.get("orderNo"));
                m.put("id", 1);
                DaoSupport.dao.autoInsertORUpdate("shipment",m);
            }

        }
    }

}
