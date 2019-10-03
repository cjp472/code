package com.lx.home.service.jea;

import com.github.pagehelper.PageHelper;
import com.lx.authority.config.OS;
import com.lx.entity.Var;
import com.lx.home.dao.Dao;
import com.lx.util.LX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by 游林夕 on 2019/9/19.
 */
@Service
public class StockService {
    @Autowired
    private Dao dao;
    public OS.Page list(Map map){
        return dao.listPage("StockMapper.list",map);
    }
    public void save(Map map) throws Exception {
        LX.exMap(map,"list");
        List<Var> ls = LX.toList(Var.class,map.get("list"));
        LX.exObj(ls,"未选择商品!");
        for (Var v : ls){
            v.putAll(map);
            v.putAll(new Var("{id=1,addTime=1,status=1}"));
            dao.autoInsertORUpdate("stock",v);
        }
    }
}
