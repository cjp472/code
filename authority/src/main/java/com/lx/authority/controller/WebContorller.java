package com.lx.authority.controller;//说明:

import com.lx.authority.config.Authority;
import com.lx.authority.config.OS;
import com.lx.authority.dao.RedisUtil;
import com.lx.entity.Var;
import com.lx.util.LX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * 创建人:游林夕/2019/4/28 14 00
 */
@RestController
@RequestMapping("/sys")
@Authority(method = true) //验证接口
public class WebContorller {
    Logger logger = LoggerFactory.getLogger(WebContorller.class);
    @Autowired
    private RedisUtil redis;
    //说明:接口调用 token登陆令牌 method为方法名
    /**{ ylx } 2019/5/14 14:14 */
    @RequestMapping(value = "/service")
    public Object service(HttpServletRequest req) throws Exception {
        Map pd = OS.getParameterMap(req);
        logger.info("--start--"+pd);
        LX.exMap(pd,"method");
        return OS.invokeMethod(pd);
    }



    @RequestMapping("/list/{main}")
    public OS.Page list(@PathVariable("main") String main) throws Exception {
        List<HashMap> ls =  redis.find("system:"+main,HashMap.class);
        ls.sort((o1,o2)->{return o1.get("id").hashCode()-o2.get("id").hashCode();});
        return new OS.Page(ls,ls.size());
    }
    @RequestMapping("/obj/{main}")
    public OS.Page obj(@RequestParam(required = false) Map map, @PathVariable("main") String main){
        LX.exMap(map,"id");
        return new OS.Page(redis.find("system:"+main,HashMap.class,map.get("id").toString()));
    }
    //说明:部分修改
    /**{ ylx } 2019/5/6 14:58 */
    @RequestMapping("/edit/{main}")
    public OS.Page edit(@RequestParam(required = false) Map map, @PathVariable("main") String main){
        if(LX.isEmpty(map.get("id"))){
            return ins(map,main);
        }
        Map old = null;
        //当id为用户写入时(不为系统生成),需要旧的id (例如:user和service)
        if (map.containsKey("old_id")){
            old = redis.find("system:"+main,HashMap.class,map.get("old_id").toString());
            redis.del("system:"+main,map.get("old_id").toString());//删除旧的id
        }else{
            old = redis.find("system:"+main,HashMap.class,map.get("id").toString());
        }
        if (old == null){
            return ins(map,main);
        }
        old.putAll(map);
        old.put("u_time", LX.getTime());
        redis.save("system:"+main,old.get("id").toString(),old);
        return new OS.Page();
    }
    //说明:全部修改
    /**{ ylx } 2019/5/6 14:58 */
    @RequestMapping("/ins/{main}")
    public OS.Page ins(@RequestParam(required = false) Map map, @PathVariable("main") String main){
        LX.exObj(map,"不能存储空的对象!");
        if (LX.isEmpty(map.get("id"))){//新增
            map.put("id",redis.getId("system:"+main,5));
        }else{
            if (redis.exists("system:"+main,map.get("id").toString())) LX.exMsg("该不能保存id相同的对象");
        }
        //全部修改
        map.put("u_time", LX.getTime());
        redis.save("system:"+main,map.get("id").toString(),map);
        return new OS.Page();
    }
    //说明:删除
    /**{ ylx } 2019/5/5 15:42 */
    @RequestMapping("/del/{main}")
    public OS.Page del(@RequestParam(required = false) Map map, @PathVariable("main") String main){
        LX.exMap(map,"id");
        String[]idss = map.get("id").toString().split(",");
        Var var = redis.find("system:"+main,Var.class,idss[0]);
        Set<String> ids = new HashSet<>(Arrays.asList(idss));
        if (var.containsKey("pid")){
            List<Var> ls =  redis.find("system:"+main,Var.class);//获取所有
            ls.sort((o1,o2)->{return o1.get("id").hashCode()-o2.get("id").hashCode();});//排序
            ls.forEach((v -> {
                if (ids.contains(v.get("pid"))){
                    ids.add(v.getStr("id"));
                }
            }));
            idss = ids.toArray(new String[ids.size()]);
        }
        redis.del("system:"+main,idss);
        return new OS.Page();
    }
    @RequestMapping("/backup")
    public void backup(){
        redis.backup();
    }

   final String updateFile = "service,menu";
    @RequestMapping("/up_version")
    public void saveFile() throws Exception {
        File file = new File("up_version.txt");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),"utf-8"));
        try {
            String v = LX.getTime();
            redis.put("system:version",v);
            pw.println(v);
            for (String uf : updateFile.split(",")){
                pw.println("system:"+uf+"_incr@~@`@"+redis.get("system:"+uf+"_incr"));
                List<HashMap> ls =  redis.find("system:"+uf,HashMap.class);
                if (LX.isNotEmpty(ls)){
                    for (HashMap h : ls){
                        pw.println("system:"+uf+"@~@`@"+h.get("id")+"@~@`@"+ LX.toJSONString(h));
                    }
                }
            }
            pw.flush();
        }finally {
            if(pw!=null) pw.close();
        }

    }
    @PostConstruct
    public void updateFile() throws Exception {
        File file = new File("up_version.txt");
        if (file.exists()){
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
            String ver = br.readLine();
            String len =null;
            String rv = redis.get("system:version");
            if (ver !=null && !ver.equals(rv)){
                //删除旧的版本
                for (String uf : updateFile.split(",")) {
                    redis.del("system:"+uf);
                    redis.del("system:"+uf+"_incr");
                }
                while ((len=br.readLine())!=null){
                    String [] pojo = len.split("@~@`@");
                    if (pojo.length==3){
                        redis.save(pojo[0],pojo[1],pojo[2]);
                    }else{
                        redis.put(pojo[0],pojo[1]);
                    }
                }
                redis.put("system:version",ver);
            }
        }


    }
}
