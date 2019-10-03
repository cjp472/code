package com.lx.service;//说明:

import com.lx.entity.WxMessage;
import com.lx.mapper.QueryMapper;
import com.lx.util.LX;
import com.lx.util.QRCodeMax;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建人:游林夕/2019/6/5 09 10
 */
@Service
public class ExcelUtilService {
    @Autowired
    private QueryMapper queryMapper;
    @Autowired
    private MyWxBot bot;

    public void ts(){
        //查询今天需要推送的信息
        List<Map> res = queryMapper.findTodayUser();
        for (Map mm : res){
            List<Map<String,String>> list = queryMapper.findDDMX(mm);
            if (LX.isNotEmpty(list)){
                try {
                    mm.put("ls",list);
                    bot.send(mm.get("user_id").toString(),QRCodeMax.creatDD(mm));
                }catch (Exception e){
                    MyWxBot.log.error("查询订单",e);
                }

            }
        }
    }
    public void save(InputStream is) throws Exception {
        List<Map<String,String>> ls = parseExcel(is);
        if (LX.isNotEmpty(ls)){//
            if (!"已结算".equals(ls.get(0).get("订单状态"))){
                List list = queryMapper.been_uploaded();//判断今天是否执行过
                if(list!=null && list.size()>0) LX.exMsg("今天已经上传过了!");
            }
            for (Map<String,String> m : ls){
                try{
                    List<WxMessage> li = LX.toList(WxMessage.class,queryMapper.updateOrderInfo(m));//更新完成结算的数据
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public List<Map<String,String>> parseExcel(InputStream is) throws Exception {
        List<Map<String,String>> list = new ArrayList<>();
        Workbook wb = new HSSFWorkbook(is);
        //开始解析
        Sheet sheet = wb.getSheetAt(0);     //读取sheet 0
        Map<Integer,String> map = new HashMap<>();//订单结算
//        String pattern = "(商品ID)|(订单编号)|(预估收入)|(效果预估)|(点击时间)|(订单状态)|(广告位ID)";
        String pattern = "(商品ID)|(淘宝订单编号)|(付款预估收入)|(点击时间)|(订单状态)|(推广位ID)";
        int ddzt = 0;
        Row row1 = sheet.getRow(0);//读取第一行
        if (row1 != null) {
            for (int i=0;i<row1.getLastCellNum();i++){
                if (row1.getCell(i).toString().matches(pattern)){
                    if ("订单状态".equals(row1.getCell(i).toString())){
                        ddzt = i;
                    }
                    map.put(i,row1.getCell(i).toString());
                }
            }
        }

        int firstRowIndex = sheet.getFirstRowNum()+1;   //第一行是列名，所以不读
        int lastRowIndex = sheet.getLastRowNum();

        for(int rIndex = firstRowIndex; rIndex <= lastRowIndex; rIndex++) {   //遍历行
            Row row = sheet.getRow(rIndex);
            if (row != null && "已结算,已付款".indexOf(row.getCell(ddzt).toString())!=-1) {
                int firstCellIndex = row.getFirstCellNum();
                int lastCellIndex = row.getLastCellNum();
                Map<String,String> m = new HashMap();
                for (int cIndex = firstCellIndex; cIndex < lastCellIndex; cIndex++) {   //遍历列
                    Cell cell = row.getCell(cIndex);
                    if (map.containsKey(cIndex)){
                        m.put(map.get(cIndex),cell.toString());
                    }
                }
                list.add(m);
            }
        }
        return list;
    }
}
