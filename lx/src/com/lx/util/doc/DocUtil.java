package com.lx.util.doc;

import com.lx.util.LX;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocUtil {
	private String classType ="";//显示指定类
	private String methodType ="";//显示指定方法
	private int isColor = 1;//显示颜色
    private Print pw;
	/**
	 *  @author
	 *  创建时间：2018年4月12日 下午12:42:10
	 */
	public static void main(String[] args) throws Exception {
        exec("C:/doc/doc.html","测试","","com.lx");
	}
	public DocUtil(String classType,String methodType){
        if (LX.isNotEmpty(classType)) this.classType = classType;
        if (LX.isNotEmpty(methodType)) this.methodType = methodType;
    }
    public static void exec(String fileName,String classType,String methodType,String...scan) throws Exception {
        LX.exObj(fileName,"文件名不能为空!");
        LX.exObj(scan,"包名不能为空!");
        if (!fileName.endsWith(".html")){
            fileName = fileName+".html";
        }
        new DocUtil(classType,methodType).classAnnotation(fileName,scan);
    }
    
   
	/**
	 * 获取class上的注解
	 *  @author 
	 *  创建时间：2018年3月4日 上午11:51:31
	 * @throws Exception 
	 */
	public  void classAnnotation(String htmlRoot,String...scan) throws Exception{
        try {
            // 获取特定包下所有的类(包括接口和类)
            List<Map<String,String>> lists = validAnnotation(scan);

            File file = new File(htmlRoot);
            if(file.exists()) {
                file.delete();
            }
            pw = new Print(htmlRoot);
            if (pw == null) {
                return;
            }
            before1();
            for (Map ppp: lists) {
                        pw.println("            <li class=\"has_children\">");
                        pw.println("                <a href=\"#one\">");
                        pw.println("                    <span>" + ppp.get("name") + "</span>");
                        pw.println("                    <span class=\"arrow_down\"></span>");
                        pw.println("                </a>");
                        pw.println("                <ul class=\"navi\">");
                        List<Map<String,String>> list = (List<Map<String,String>>) ppp.get("list");
                        for (Map<String,String> pp : list) {
                            pw.println("                    <li><a href=\"#" + pp.get("method") + "\"><span>" + pp.get("name") + "</span></a></li>");
                        }
                        pw.println("                </ul>");
                        pw.println("            </li>");
                    }
                after1();
                before2();
                for (Map pp : lists) {
                    List<Map> list = (List<Map>) pp.get("list");
                    for (Map<String,String> p : list) {
                        String[] arr = {};
                        String[] out = {};
                        if (LX.isNotEmpty(p.get("in"))) {
                            arr = p.get("in").split(",");
                        }
                        if (LX.isNotEmpty(p.get("out"))) {
                            out = p.get("out").split(",");
                        }
                        String url = p.get("url");
                        String method = "";
                        if (LX.isEmpty(url)) {
                            url = "";
                        }
                        if (url.lastIndexOf("/") != -1) {
                            method = url.substring(url.lastIndexOf("/"));
                        } else {
                            method = url;
                        }
                        setTable(pp.get("id").toString(), pp.get("name").toString(), pp.get("cName").toString(), p.get("method"), p.get("name"),
                                method, url, arr, out, p.get("msg"), p.get("result"));
                    }
                }
                after2();
        }catch(Exception e){
               e.printStackTrace();
                LX.error(e);
            }finally{
                if(pw != null)
                    pw.close();
            }
    }
	/**获取类上的注解*/
	public List<Map<String,String>> validAnnotation(String...scan) throws Exception {
        // 获取特定包下所有的类(包括接口和类)
        List<Map<String,String>> lists = new ArrayList<Map<String,String>>();
        List<Class<?>> clsList = ScanPackage.scan(scan);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                boolean boo = cls.isAnnotationPresent(Doc.class);
                if (boo) {
                    String name = "";
                    String type = "";
                    //3.1获取类上的所有注释
                    Annotation[] annos = cls.getAnnotations();
                    boolean na = false;
                    for (Annotation a : annos) {
                        if (LX.isEmpty(a)) continue;
                        if (a instanceof Doc) {//判断注解
                            Doc doc = (Doc) a;
                            name = doc.name();
                            type = doc.type();

                        } else if (a instanceof Deprecated) {
                            na = true;//已过时
                        }
                    }
                    boolean b = false;
                    if(LX.isNotEmpty(classType)){//判断类是否符合要求
                        for (String str : classType.split(",")){
                            if (str.equals(type.trim())){
                                b = true;
                                break;
                            }
                        }
                    }else{
                        b = true;
                    }
                    if (!b) {
                        continue;//类不是指定类型
                    }
                    if (na) {
                        name = "<del  class='red'>" + name + "</del>";
                    }
                    Map p = new HashMap<>();//用来存储类
                    //获取所有方法
                    List<Map<String,String>> list = validAnnotation(cls);
                    if (LX.isEmpty(list)) continue;
                    p.put("list", list);
                    p.put("name", name);
                    p.put("id", cls.getName().replace(".", ""));
                    p.put("cName", cls.getSimpleName());
                    lists.add(p);
                }
            }
        }
        return lists;
    }
	/**
	 * 获取方法上的注解
	 *  @author 
	 *  创建时间：2018年3月4日 上午11:51:56
	 */
	public  List<Map<String,String>> validAnnotation(Class<?> cls) throws Exception {
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
        //获取类中的所有的方法  
        Method[] methods = cls.getDeclaredMethods();  
        for (Method method : methods) {
        	
        	//3.1获取方法上的所有注释
        	Annotation[] annotations = method.getAnnotations();
        	String name = "";
			String url = "";
			String in = "";
			String out = "";
			String msg = "";
			boolean show = false;
            boolean na = false;
        	for(Annotation a : annotations){
        		//如果是@Doc注释，则强制转化，
        		
        		if(LX.isEmpty(a)) continue;
        		if(a instanceof Doc){
        			Doc doc = (Doc) a;
        			String type = doc.type();
        			if(LX.isNotEmpty(methodType)){
                        for (String str : methodType.split(",")){
                            if (str.equals(type.trim())){
                                show = true;
                                break;
                            }
                        }
        			}else{
                        show = true;
                    }

    				name = doc.name();
    				url = doc.method();
    				in = doc.in();
    				out = doc.out();
    				msg = doc.msg();
        		}else if(a instanceof Deprecated){
                    na = true;
        		}
        	}
            if(na) name = "<del class='red'>"+name+"</del>";//将方法表示为已过时
        	boolean boo = method.isAnnotationPresent(Doc.class);
        	if(boo&&show){
                Type type=method.getGenericReturnType();
                String t=type.toString();
                if (t.toLowerCase().indexOf("list")!=-1){
                    msg = "返回值存在:rows中\n"+msg;
                }else{
                    msg = "返回值存在:entity中\n"+msg;
                }
                Map<String,String> pp = new HashMap<String,String>();
                pp.put("method", (cls.getName()+"["+method.getName()+"]"));
                pp.put("mName", method.getName());
                String result = "";
                String string =  getMsg1(new StringBuilder(),result,"                     \t");
                pp.put("result",string);
                pp.put("name", name);
                pp.put("url", url);
                pp.put("in", in);
                pp.put("out", out);
                pp.put("msg", msg);
                LX.exObj(url,"方法名没有设置:"+pp.get("method"));
        		list.add(pp);
        	}
		}
        return list;
    }  
	
	/**
	 * api列表显示
	 * @param id
	 * @param className
	 * @param methodId
	 * @param methodName
	 * @param method
	 * @param url
	 * @param in
	 * @param out
	 * @param msg
	 * @param result
	 */
	public void setTable(String id , String className ,String cName , String methodId , String methodName ,
			String method , String url ,String[]  in ,String[]  out , String msg , String result){
		pw.println("    <h5 id=\""+id+"\">"+className+"("+cName+")</h5>");
		pw.println("    <div  class=\"list\" id=\""+methodId+"\">");
		pw.println("        <h1 class='blue'>"+methodName+"  (<span class='red'>"+method+"</span>) </br> 类名+方法名:"+methodId+"</h1>");
		pw.println("        <ul>");
		pw.println("            <li>返回类型：JSON</li>");
		pw.println("            <li class='tdss1'>调用地址示例："+url+"</li>");
		pw.println("            <li>");
		pw.println("                <table>");
		pw.println("                    <thead>");
		pw.println("                        <tr class='trss'>");
		pw.println("                            <td class=\"font1\">参数名称</td>");
		pw.println("                            <td class=\"font1\">中文名称</td>");
		pw.println("                            <td class=\"font1\">输入输出</td>");
		pw.println("                            <td class=\"font3\">参数说明</td>");
		pw.println("                        </tr>");
		pw.println("                    </thead>");
		pw.println("                    <tbody>");
		for (String string : in) {
			String [] ar = string.split("=");
			pw.println("                    <tr class='tds1'>");
			pw.println("                        <td class=\"font1\">"+ar[0]+"</td>");
			pw.println("                        <td class=\"font1\">"+(ar.length<2?"":ar[1])+"</td>");
			pw.println("                        <td class=\"font1\">in</td>");
			pw.println("                        <td class=\"font3\">"+(ar.length<3?"":ar[2])+"</td>");
			pw.println("                    </tr>");
		}
		for (String string : out) {
			String [] ar = string.split("=");
			pw.println("                    <tr  class='tds2'>");
			pw.println("                        <td class=\"font1\">"+ar[0]+"</td>");
			pw.println("                        <td class=\"font1\">"+(ar.length<2?"":ar[1])+"</td>");
			pw.println("                        <td class=\"font1\">out</td>");
			pw.println("                        <td class=\"font3\">"+(ar.length<3?"":ar[2])+"</td>");
			pw.println("                    </tr>");
		}
		pw.println("                    </tbody>");
		pw.println("");
		pw.println("                </table>");
		pw.println("            </li>");
		pw.println("            <li>说明:"+(LX.isEmpty(msg)?"":msg.replace("\n","</br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"))+"</li>");
		pw.println("            <li>");
		pw.println("                返回结果样式：</br>");
		pw.println("                <pre>");
		pw.println("                     "+"\n"+result);
		pw.println("                </pre>");
		pw.println("            </li>");
		pw.println("        </ul>");
		pw.println("    </div>");
	}
	
//	public static void main(String[] args) {
//		String string = "[{DoctorName=乔震, hj=0.0000, inPay=68815.9300, outPay=68815.9300, DeptName=全科（本部）}, {DoctorName=谭继英, hj=0.0000, inPay=7814.2800, outPay=7814.2800, DeptName=全科（本部）}, {DoctorName=刘宗起, hj=0.0000, inPay=4743.3700, outPay=4743.3700, DeptName=全科（本部）}, {DoctorName=胡雨, hj=0.0000, inPay=61122.2400, outPay=61122.2400, DeptName=全科（本部）}, {DoctorName=刘春环, hj=0.0000, inPay=26792.3800, outPay=26792.3800, DeptName=预防保健科（本部）}, {DoctorName=高艳芳, hj=0.0000, inPay=11651.7500, outPay=11651.7500, DeptName=口腔科（流芳台）}, {DoctorName=纪严军, hj=0.0000, inPay=42269.6900, outPay=42269.6900, DeptName=中医科（流芳台）}, {DoctorName=李静, hj=0.0000, inPay=12253.3600, outPay=12253.3600, DeptName=妇科（流芳台）}, {DoctorName=郭玉军, hj=0.0000, inPay=3879.0000, outPay=3879.0000, DeptName=预防保健科（流芳）}, {DoctorName=魏巍, hj=0.0000, inPay=10021.0000, outPay=10021.0000, DeptName=预防保健科（流芳）}, {DoctorName=刘春环, hj=0.0000, inPay=554.0000, outPay=554.0000, DeptName=预防保健科（流芳）}, {DoctorName=董光霞, hj=0.0000, inPay=127562.2800, outPay=127562.2800, DeptName=全科（詹滨西里）}, {DoctorName=魏忠宝, hj=0.0000, inPay=10145.8700, outPay=10145.8700, DeptName=全科（流芳台）}, {DoctorName=郭玉军, hj=0.0000, inPay=99807.6800, outPay=99807.6800, DeptName=全科（流芳台）}, {DoctorName=段晓峰, hj=0.0000, inPay=39439.2100, outPay=39439.2100, DeptName=全科（流芳台）}]";
//		System.out.println(new DocUtil().getMsg1(new StringBuilder(),string,"\t"));;
//	}

    //将List 和Map<String,String> 对象拼接为指定格式的字符串
	public String getMsg1(StringBuilder sb , String str , String wy) {
		
		if(LX.isEmpty(wy)) wy = "";
		if(LX.isEmpty(str)) return sb.toString();
		str = str.replace(" ", "");
		str=str.trim();
		if (str.startsWith("[")) {
			int y = 1;
//			do{
				if(y++==1){
					sb.append(wy).append("[\n");
				}else{
					sb.append(wy).append(",[\n");
				}
				str = getMsg1(sb,str.substring(1),wy+"\t").trim().substring(1);
				sb.append(wy).append("]\n");
				if(LX.isEmpty(str)) return sb.toString();
				return str;
//			}while(LX.isNotEmpty(str.trim()));
		}else if (str.startsWith("{")) {
			int z = 1;
			while(true){
				int k = str.indexOf("{");
				if(k!=0&&k!=1){
					if(LX.isEmpty(str)) return sb.toString();
					return str;
				}
				if(z++==1){
					sb.append(wy).append("{\n");
				}else{
					sb.append(wy).append(",{\n");
				}
				
				str = str.substring(k+1).trim();
				int i = -1;
				int b = -1;
				String wy1 = "\t"+wy;
				do{
					i=str.indexOf("=");
					b=str.indexOf("}");
					if (i == -1 || b<i) {
						sb.append(wy).append(str.substring(0,b+1)+"\n");
						str = str.substring(b+1).trim();
						break;
					}
					char a = str.charAt(i+1);
					if (a=='{'||a=='[') {
						sb.append(wy1).append(str.substring(0,i+1)).append("\n");
						str = getMsg1(sb,str.substring(i+1), "\t\t"+wy).trim();
//						sb.append(wy).append("},");
					}else{
						int j = str.indexOf(",");
						if(j==-1||j>b){
							sb.append(wy1).append(str.substring(0,b)).append("\n");
							str = str.substring(b).trim();
							continue;
						}
						sb.append(wy1).append(str.substring(0,j+1)).append("\n");
						str = str.substring(j+1).trim();
					}
					
				}while(i!=-1);
			}
			
			
			
		}else {
			return sb.toString();
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	public void before1() {
		pw.println("<!DOCTYPE html>");
		pw.println("<html>");
		pw.println("<head lang=\"en\">");
		pw.println("    <meta charset=\"UTF-8\">");
		pw.println("    <title>文档</title>");
		pw.println("    <style>");
		pw.println("        body,html{");
		pw.println("            width: 1190px;");
		pw.println("            margin: 0 auto;");
		pw.println("            position: relative;");
		pw.println("            background-color: #d6d6d6;");
		pw.println("        }");
		pw.println("        .clearfix {");
		pw.println("            *zoom: 1;");
		pw.println("        }");
		pw.println("        .clearfix:after {");
		pw.println("            content: \"\";");
		pw.println("            display: block;");
		pw.println("            clear: both;");
		pw.println("            height: 0;");
		pw.println("        }");
		pw.println("        ul, li{");
		pw.println("            margin: 0;");
		pw.println("            padding: 0;");
		pw.println("            list-style: none;");
		pw.println("            font-weight: 300;");
		pw.println("        }");
		pw.println("        #header{");
		pw.println("            position: fixed;");
		pw.println("            width: 100%;");
		pw.println("            height: 80px;");
		pw.println("            top: 0;");
		pw.println("            left: 0;");
		pw.println("            background:rgba(255,255,255,.1)");
		pw.println("            z-index: 9999999;");
		pw.println("            transition: all .25s ease-in-out;");
		pw.println("        }");
		pw.println("        #content{");
		pw.println("            position: relative;");
		pw.println("            overflow: hidden;");
		pw.println("            text-align: center;");
		pw.println("            width: 100%;");
		pw.println("           height: 80px;");
		pw.println("        }");
		pw.println("        #img{");
		pw.println("            width:110px;");
		pw.println("            height: auto;");
		pw.println("            position: absolute;");
		pw.println("            right: 8%;");
		pw.println("            top:50%;");
		pw.println("            transform: translate(0,-50%);");
		pw.println("        }");
		pw.println("        .header_menu {");
		pw.println("            cursor: pointer;");
		pw.println("            height: auto;");
		pw.println("            width: auto;");
		pw.println("            z-index: 9999999;");
		pw.println("            display:inline-block;");
		pw.println("            position:absolute;");
		pw.println("            background-color: rgba(246,246,246,0.4);");
		pw.println("            padding: 5px;");
		pw.println("            left:8%;");
		pw.println("            top:50%;");
		pw.println("            transform: translate(0,-50%);");
		pw.println("        }");
		pw.println("        .header_menu span{");
		pw.println("            cursor: pointer;");
		pw.println("            position: relative;");
		pw.println("            display: inline-block;");
		pw.println("            width: 24px;");
		pw.println("            height: 2px;");
		pw.println("            background-color: #272727;");
		pw.println("            vertical-align: middle;");
		pw.println("            transition-duration: 0.3s, 0.3s;");
		pw.println("            transition-delay: 0.3s, 0s;");
		pw.println("            margin-top: -4px;");
		pw.println("            text-align: left;");
		pw.println("        }");
		pw.println("        .header_menu span:before,");
		pw.println("        .header_menu span:after {");
		pw.println("            content:\"\";");
		pw.println("            cursor: pointer;");
		pw.println("            position: absolute;");
		pw.println("            display: inline-block;");
		pw.println("            width: 24px;");
		pw.println("            height: 2px;");
		pw.println("            background-color: #272727;");
		pw.println("            transition-duration: 0.3s, 0.3s;");
		pw.println("            transition-delay: 0.3s, 0s;");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu span:before {");
		pw.println("            top: -7px;");
		pw.println("            transition-property: top, transform;");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu span:after {");
		pw.println("            bottom: -7px;");
		pw.println("            transition-property: bottom, transform;");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu.active span {");
		pw.println("            background-color: transparent;");
		pw.println("            transition-delay: 0s, 0s;");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu.active span:before,");
		pw.println("        .header_menu.active span:after {");
		pw.println("            transition-delay: 0s, 0.3s;");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu.active span:before {");
		pw.println("            top: 0;");
		pw.println("            background: #4c4c4c;");
		pw.println("            transform: rotate(45deg);");
		pw.println("        }");
		pw.println("");
		pw.println("        .header_menu.active span:after {");
		pw.println("            bottom: 0;");
		pw.println("            background: #4c4c4c;");
		pw.println("            transform: rotate(-45deg);");
		pw.println("        }");
		pw.println("        #main_menu{");
		pw.println("            display: none;");
		pw.println("        }");
		pw.println("        .main_menu{");
		pw.println("            position:fixed;");
		pw.println("            background-color: #f6f6f6;");
		pw.println("            left:0;");
		pw.println("            top:80px;");
		pw.println("            width:25%;");
		pw.println("            min-width:246px;");
		pw.println("            bottom: 0;");
		pw.println("            overflow: auto;");
		pw.println("        }");
		pw.println("        .main_menu a{");
		pw.println("            padding:18px 0 18px 45px;");
		pw.println("            display: block;");
		pw.println("            line-height:1;");
		pw.println("            font-size:16px;");
		pw.println("        }");
		pw.println("        .main_menu a .arrow_down{");
		pw.println("            background: url(http://www.ttxinyi.com:11080/TXApp//pchtml/img/doc/next2.png);");
		pw.println("            background-size: 100% 100%;");
		pw.println("            display: inline-block;");
		pw.println("            width: 10px;");
		pw.println("            height:20px;");
		pw.println("            float: right;");
		pw.println("            margin-left: 10px;");
		pw.println("            margin-right: 25px;");
		pw.println("            transition:all .3s ease-out;");
		pw.println("        }");
		pw.println("        .main_menu a:hover,");
		pw.println("        .main_menu a.active{background-color: #ffffff;}");
		pw.println("        .main_menu a.open .arrow_down{");
		pw.println("            transform:rotate(90deg);");
		pw.println("        }");
		pw.println("        .main_menu ul ul a{");
		pw.println("            padding-left:65px;");
		pw.println("        }");
		pw.println("        .main_menu > ul > li span {");
		pw.println("            display: inline-block;");
		pw.println("        }");
		pw.println("        .main_menu ul.navi{");
		pw.println("            display:none;");
		pw.println("            overflow:hidden;");
		pw.println("        }");
		pw.println("        .header.smaller_header{padding-top:12px;padding-bottom:12px;}");
		pw.println("        .header.smaller_header .main_menu{top:77px;}");
		pw.println("        #section{");
		pw.println("            width: 100%;");
		pw.println("            margin-top: 100px;");
		pw.println("        }");
		pw.println("        #section>.list>ul{");
		pw.println("            border: 1px solid #000;");
		pw.println("            background: #fff;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li:not(:first-child){");
		pw.println("            border-top: 1px solid #000;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li:not(:nth-child(3)){");
		pw.println("            padding: 10px 20px;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li>table tr>td{");
		pw.println("            padding: 10px 2px;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li>table>thead>tr>td{");
		pw.println("            border-bottom: 1px dashed #000;");
//		pw.println("            background-color: #f6f6f6;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li>table tr:not(:last-child)>td{");
		pw.println("            border-bottom: 1px dashed #000;");
		pw.println("        }");
		pw.println("        #section>.list>ul>li>table tr>td:not(:first-child){");
		pw.println("            border-left: 1px dashed #000;");
		pw.println("        }");
		pw.println("        table{width:100%;}");
		pw.println("        .font1{");
		pw.println("            width: 20%;");
		pw.println("            text-align: center;");
		pw.println("        }");
		pw.println("        .font3{");
		pw.println("            width: 35%;");
		pw.println("            text-align: center;");
		pw.println("        }");
		pw.println("        pre{");
		pw.println("            display: none;");
		pw.println("        }");
        pw.println("        font20{");
        pw.println("            font-size: 20px;");
        pw.println("        }");
		if (isColor==1) {
			pw.println("        .red{color:red;}");
			pw.println("        .blue{color:blue;}");
			
			pw.println("        .tdss{background-color:#ff9966;}");
			pw.println("        .tdss1{background-color:#ffcc77;}");
			pw.println("        .trss{background-color:#ffcc99;}");
			pw.println("        .tds1{background-color:#ffffcc;}");
			pw.println("        .tds2{background-color:#99ccff;}");
		}
		pw.println("    </style>");
		pw.println("</head>");
		pw.println("<body>");
		pw.println("<header id=\"header\">");
		pw.println("	");
		pw.println("    <div id=\"content\">  ");
		pw.println("		<div class=\"logo\">");
		pw.println("            <img id=\"img\" src=\"http://www.ttxinyi.com:11080/TXApp//pchtml/img/doc/logo.png\" alt=\"\"/>");
		pw.println("        </div>	");
		pw.println("        <div class=\"header_menu\" id=\"header_menu\">");
		pw.println("            <span class=\"menu_toggle\"></span>");
		pw.println("        </div>");
		pw.println("    </div>");
		pw.println("    <nav class=\"main_menu active\" id=\"main_menu\">");
		pw.println("        <ul>");
	}
	
	public void after1() {
		pw.println("        </ul>");
		pw.println("    </nav>");
		pw.println("</header>");
	}
	
	public void before2() {
		pw.println("<section id=\"section\">");
	}
	public void after2() {
		pw.println("</section>");
		pw.println("<script src=\"https://libs.baidu.com/jquery/1.10.2/jquery.min.js\"></script>");
		pw.println("<script>");
		pw.println("    $('#header_menu').click(function () {");
		pw.println("        if($(this).attr(\"class\").indexOf('active')!=-1){");
		pw.println("            $(this).removeClass(\"active\");");
		pw.println("            $('#main_menu').hide();");
		pw.println("        }else{");
		pw.println("            $(this).addClass('active');");
		pw.println("            $('#main_menu').show();");
		pw.println("        }");
		pw.println("    });");
		pw.println("    $('#main_menu li.has_children').children('a').click(function () {");
		pw.println("        $('.navi').slideUp();");
		pw.println("        $('#main_menu li.has_children').children('a').removeClass('open');");
		pw.println("        var _this=$(this);");
		pw.println("        var _ulobj=_this.next();");
		pw.println("        var _isvisible=_ulobj.is(':visible');");
		pw.println("        if(_isvisible){");
		pw.println("            _ulobj.slideUp('fast');");
		pw.println("        }else {");
		pw.println("            _ulobj.slideDown();");
		pw.println("            _this.addClass('open');");
		pw.println("        }");
		pw.println("    });");
		pw.println("    $(\".navi a\").click(function(){");
		pw.println("        $(\"#main_menu\").hide();");
		pw.println("        $(\"#header_menu\").removeClass(\"active\");");
		pw.println("    });");
		pw.println("    $(\"pre\").parent().click(function(){");
		pw.println("        $(this).children('pre').show(300);");
		pw.println("        $(this).mouseleave(function(){");
		pw.println("            $(\"pre\").hide(300);");
		pw.println("        })");
		pw.println("    })");
		pw.println("</script>");
		pw.println("</body>");
		pw.println("</html>");
	}
}
