package com.lx.home.service.jea;

import com.lx.home.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by 游林夕 on 2019/9/29.
 */
@Service
public class ConsoleService {

    @Autowired
    private Dao dao;

    public Map console(Map map){
        dao.findforObj("ConsoleMapper.head",map);
        dao.findforList("ConsoleMapper.daily_sales_volume",map);
        return null;
    }

}
