package com.lx.util;//说明:

import javax.script.Invocable;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;

import static com.lx.util.LX.*;


/**
 * 创建人:游林夕/2019/3/27 17 35
 */
public class MathUtil {

    public enum Type{
        /**大于 小于 等于 大于等于 小于等于*/
        GT,LT,EQ,GTEQ,LTEQ
    }
    static boolean compareTo(Object obj1 , Object obj2,Type c){
        switch (c){
            case GT:
                return compareTo(obj1,obj2)>0;
            case LT:
                return compareTo(obj1,obj2)<0;
            case EQ:
                return compareTo(obj1,obj2)==0;
            case GTEQ:
                return compareTo(obj1,obj2)>=0;
            case LTEQ:
                return compareTo(obj1,obj2)<=0;
            default: return false;
        }
    }
    /**比较两个数字的大小*/
    static int compareTo(Object obj1 , Object obj2) {
        return getBigDecimal(obj1).compareTo(getBigDecimal(obj2));
    }
    /**计算*/
    static BigDecimal eval(String str){
        String result = null;
        try {
            Invocable ic = (Invocable) new ScriptEngineManager().getEngineByName("javascript");
            result = str(ic.invokeFunction("eval",str));
            if (!result.matches("(-?\\d+)(\\.\\d+)?")) exMsg("");
        } catch (Exception e) {
            exMsg("无法计算该算式的值:"+str);
        }
        return getBigDecimal(result);
    }
}
