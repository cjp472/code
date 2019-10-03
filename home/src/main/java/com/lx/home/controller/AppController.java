package com.lx.home.controller;//说明:

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lx.authority.config.Authority;
import com.lx.authority.config.OS;
import com.lx.entity.Var;
import com.lx.home.dao.Dao;
import com.lx.home.service.jea.ShipmentService;
import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 创建人:游林夕/2019/4/24 17 12
 */
@RestController
@RequestMapping(value="/app")
//@Authority()
public class AppController {

    @Autowired
    private Dao dao;
    @Autowired
    private ShipmentService shipmentService;
    Logger logger = LoggerFactory.getLogger(AppController.class);

    @RequestMapping("/page/{main}")
    public Object page(@RequestParam(required = false) Map<Object,Object> map,@PathVariable("main") String main) throws Exception {
        for (Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();it.hasNext();){
            Map.Entry<Object, Object> entry=it.next();
            if(LX.isEmpty(entry.getValue())){
                it.remove();
            }
        }
        return dao.page(main,map);
    }
    @RequestMapping("/list/{main}")
    public Object list(@RequestParam(required = false) Map<Object,Object> map,@PathVariable("main") String main) throws Exception {
        return new OS.Page(dao.selectAll(main,map));
    }
    @RequestMapping("/obj/{main}")
    public Object obj(@RequestParam(required = false) Map map, @PathVariable("main") String main) throws Exception {
        return new OS.Page(dao.selectOne(main,map));
    }
    //说明:部分修改
    /**{ ylx } 2019/5/6 14:58 */
    @RequestMapping("/edit/{main}")
    public Object edit(@RequestParam(required = false) Map map,@PathVariable("main") String main) throws Exception {
        dao.autoInsertORUpdate(main,map);
        return new OS.Page();
    }
    //说明:删除
    /**{ ylx } 2019/5/5 15:42 */
    @RequestMapping("/del/{main}")
    public Object del(@RequestParam(required = false) Map map,@PathVariable("main") String main) throws Exception {
        dao.delete(main,map);
        return new OS.Page();
    }
    //说明:删除
    /**{ ylx } 2019/5/5 15:42 */
    @RequestMapping("/delAll/{main}")
    public Object delAll(@RequestParam(required = false) Map map,@PathVariable("main") String main) throws Exception {
        LX.exMap(map,"ls");
        List<Var> ls = LX.toList(Var.class,map.get("ls"));
        if (LX.isNotEmpty(ls)){
            for(Var v :ls){
                dao.delete(main,v);
            }
        }
        return new OS.Page();
    }

    @RequestMapping(value = "/list")
    @ResponseBody
//    @Authority(method = true)
    public OS.Page list(HttpServletRequest req) throws Exception {
        Map pd = OS.getParameterMap(req);
        logger.info("--start--" + pd);
        LX.exMap(pd, "method");
        return (OS.Page) OS.invokeMethod(pd);
    }
    @RequestMapping(value = "/service")
    @ResponseBody
//    @Authority(method = true)
    public Object service(HttpServletRequest req) throws Exception {
        Map pd = OS.getParameterMap(req);
        logger.info("--start--"+pd);
        LX.exMap(pd,"method");
        Object res = OS.invokeMethod(pd);
        logger.info(LX.str(res));

        if (res instanceof List){//返回值是List
            return new OS.Page((List)res);
        }else{//返回值是空或是Map
            if (res==null || res instanceof Map){
                return new OS.Page((Map)res);
            }else{
                return new OS.Page(LX.toMap(res));
            }
        }
    }
    @RequestMapping("/login")
    @ResponseBody
    @Authority(false)//不进行登录验证
    public Object login(@RequestParam(required = false) Map pd, HttpServletRequest req) throws Exception {
        logger.info("--请求登陆--"+pd);
        LX.exMap(pd,"name,sign,time");
        String token = OS.login(req,pd.get("name").toString(),null,(t)->{
            return (LX.isNotEmpty(t)&&((String)pd.get("sign")).equalsIgnoreCase(LX.md5(t.get(OS.USERNAME)+","+t.get(OS.PASSWORD)+","+pd.get("time"))));
        });
        logger.info("--登陆成功--");
        return new OS.Page(LX.toMap("{'token'='{0}'}",token));
    }

    @RequestMapping("/fileUpload")
    @ResponseBody
    public Map fileUpload(@RequestParam("file") MultipartFile file) throws Exception {
        shipmentService.parseCSV(file.getInputStream());
        return LX.toMap("{msg=success}");
    }
}
