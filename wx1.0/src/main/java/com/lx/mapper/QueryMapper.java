package com.lx.mapper;//说明:

import java.util.List;
import java.util.Map;

/**
 * 创建人:游林夕/2019/6/6 17 50
 */
public interface QueryMapper {

    //说明:根据用户和商品id查询 如果用户查询过就直接返回 否则返回一个pid
    /**{ ylx } 2019/6/6 17:51 */
    Map queryPID(Map map);

    //说明:保存查询消息
    /**{ ylx } 2019/6/6 19:26 */
    Map saveQueryInfo(Map map);
    //说明:查询昨天的信息是否保存过
    /**{ ylx } 2019/6/7 19:05 */
    Map selectYesterdayInfo();
    //说明:更新和确认订单信息
    /**{ ylx } 2019/6/7 19:05 */
    Map updateOrderInfo(Map<String, String> m);
    //说明:查询今天是否执行过付款订单
    /**{ ylx } 2019/6/8 10:10 */
    List<Map> been_uploaded();
    //说明:查询今天付款和确认的人的信息
    /**{ ylx } 2019/6/17 14:24 */
    List<Map> findTodayUser();
    //说明:查询提现信息
    /**{ ylx } 2019/6/8 11:41 */
    Map findTXInfo(Map map);
    //说明:查询订单明细
    /**{ ylx } 2019/6/8 16:16 */
    List<Map<String,String>> findDDMX(Map map);
}
