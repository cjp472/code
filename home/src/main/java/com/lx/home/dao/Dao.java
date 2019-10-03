package com.lx.home.dao;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lx.authority.config.OS;
import com.lx.util.LX;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("dao")
public class Dao{

	@Resource(name = "sqlSessionTemplate")
	private SqlSessionTemplate sqlSessionTemplate;

    public int exec(String str, Object obj) {
        if (!str.startsWith("mapper.")){ str = "mapper."+str;}
        return sqlSessionTemplate.update(str, obj);
    }
    public <T>T findforObj(String str, Object obj) {
        if (!str.startsWith("mapper.")){ str = "mapper."+str;}
        return sqlSessionTemplate.selectOne(str, obj);
    }
    public <E>List<E> findforList(String str, Object obj)  {
        if (!str.startsWith("mapper.")){ str = "mapper."+str;}
        return sqlSessionTemplate.selectList(str, obj);
    }
    public OS.Page listPage(String str, Map map){
        LX.exMap(map,"page,limit");
        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        List ls = findforList(str,map);
        PageInfo page = new PageInfo(ls);
        return new OS.Page(ls,page.getTotal());
    }

    public OS.Page page(String tableName , Map  pd) throws Exception {
        LX.exMap(pd,"page,limit,lxOrder");
        pd.put("lxOrder",LX.format("order={0},page={1},limit={2}",pd.get("lxOrder"),pd.get("page"),pd.get("limit")));
        List<Map> count = (List<Map>) concatStr(tableName, pd, "count");
        LX.exObj(count,"未获取到分页信息!");
        List<Map> page = (List<Map>) concatStr(tableName, pd, "page");
        return  new OS.Page(page,(int)count.get(0).get("count"));
    }
    public int autoInsertORUpdate(String tableName ,Map  pd) throws Exception {
        return (int) concatStr(tableName,pd,"auto");
    }
    public int insert(String tableName ,Map  pd) throws Exception {
        return (int) concatStr(tableName,pd,"insert");
    }
    public int update(String tableName ,Map pd) throws Exception {
        return (int) concatStr(tableName,pd,"update");
    }
    public int delete(String tableName ,Map pd) throws Exception {
        return (int) concatStr(tableName,pd,"delete");
    }
    public Map selectOne(String tableName ,Map pd) throws Exception {
        List<Map> list = selectAll(tableName,pd);
        if (list!=null && list.size()>0)
            return list.get(0);
        return null;
    }
    public List<Map> selectAll(String tableName ,Map pd) throws Exception {
        return (List<Map>) concatStr(tableName,pd,"select");
    }
    private Object concatStr(String tableName , Map<Object,Object> pd,String sqlType) throws Exception {
        LX.exObj(tableName,"请传入要操作的表名");
        Map para = new HashMap();
        para.put("table",tableName);
        StringBuilder parm = new StringBuilder(10000);
        StringBuilder where = new StringBuilder(10000);
        if (pd != null){
            for (Map.Entry entry: pd.entrySet()){
                if (entry.getKey()!=null){
                    if(entry.getKey().toString().startsWith("lx_")){
                        String key = entry.getKey().toString().substring(3);
                        if (where.length() == 0)
                            where.append(key).append("@=").append(entry.getValue());
                        else
                            where.append("@,").append(key).append("@=").append(entry.getValue());
                    }else if(entry.getKey().toString().equals("@1")){
                        if (where.length() == 0)
                            where.append("@1").append("@=").append(entry.getValue());
                        else
                            where.append("@,").append("@1").append("@=").append(entry.getValue());
                    }else if(entry.getKey().toString().equals("@2")){
                        if (parm.length() == 0)
                            parm.append("@1").append("@=").append(entry.getValue());
                        else
                            parm.append("@,").append("@1").append("@=").append(entry.getValue());
                    } else {
                        if (parm.length() == 0)
                            parm.append(entry.getKey()).append("@=").append(entry.getValue());
                        else
                            parm.append("@,").append(entry.getKey()).append("@=").append(entry.getValue());
                    }
                }
            }
        }
        para.put("parm",parm.toString());
        para.put("where",where.toString());
        para.put("order",pd.get("lxOrder"));
        para.put("sqlType",sqlType);
        return concatStr(para);
    }
    private Object concatStr(Map para) throws Exception {
        String sqlType = para.get("sqlType").toString();
        List<Map> ls = null;
        if ("auto".equals(sqlType))//自动更新或插入
            ls = findforList("daoUtilMapper.autoInsertORUpdate",para);
        else
            ls = findforList("daoUtilMapper.selects",para);//执行查询

        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        if (LX.isNotEmpty(ls)){
            Map p = ls.get(0);
            if ("@0".equals(p.get("sqlStatus"))){
                throw new Exception(p.get("msg").toString());
            }
        }

        if ("select,count,page".indexOf(sqlType)==-1){
            LX.exObj(ls,"查询出现问题!请联系管理员!");
            Map p = ls.get(0);
            return Integer.parseInt(p.get("num").toString());
        }
        return ls==null?new ArrayList<Map>():ls;
    }
}


