package com.lx.util.doc;//说明:

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 创建人:游林夕/2019/6/10 13 13
 */
public class ScanPackage {
    public static List<Class<?>> scan(String...scans) throws IOException {
        List<Class<?>> beans = new ArrayList<>();
        for (String page:scans){//多个包
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(page.replace(".", "/"));//获取当前包的所有类信息
            while (dirs.hasMoreElements()){
                URL url = dirs.nextElement();// 获取下一个元素
                String protocol = url.getProtocol();// 得到协议的名称
                if ("file".equals(protocol)) {// 如果是以文件的形式保存在服务器上
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");// 获取包的物理路径
                    findAndAddClassesInPackageByFile(page,filePath,beans);
                }else if ("jar".equals(protocol)) {//查询jar包里的类
                    findAndAddClassByjar(url,page.replace('.', '/'),page,beans);
                }
            }
        }
        return beans;
    }

    private static void findAndAddClassByjar(URL url,String packageDirName,String packageName,List<Class<?>> classes){
        JarFile jar;// 如果是jar包文件
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();// 获取jar
            Enumeration<JarEntry> entries = jar.entries();// 从此jar包 得到一个枚举类
            while (entries.hasMoreElements()) {// 同样的进行循环迭代
                JarEntry entry = entries.nextElement();// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                String name = entry.getName();
                if (name.charAt(0) == '/') {// 如果是以/开头的
                    name = name.substring(1);// 获取后面的字符串
                }
                if (name.startsWith(packageDirName)) {// 如果前半部分和定义的包名相同
                    int idx = name.lastIndexOf('/');
                    if ((idx != -1)) {// 如果可以迭代下去 并且是一个包
                        packageName = name.substring(0, idx).replace('/', '.');// 获取包名 把"/"替换成"."
                        if (name.endsWith(".class") && !entry.isDirectory()) {// 如果是一个.class文件 而且不是目录
                            String className = name.substring(packageName.length() + 1, name.length() - 6);// 去掉后面的".class" 获取真正的类名
                            try {
                                classes.add(Class.forName(packageName + '.' + className));// 添加到classes
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
