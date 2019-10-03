package com.lx.util.doc;

import com.lx.util.LX;

import java.io.*;

class Print {
	private  PrintWriter print = null;
	private String name = null;
	public PrintWriter getPrint() {
		return print;
	}
	
	/**
	 * 将流改变
	 * @param fileName
	 * @return
	 */
	public void setPrinter(String fileName){
        LX.exObj(fileName, "文件名不能为空");
		if(!fileName.equals(name)||print == null){
			synchronized (Print.class){
				if(!fileName.equals(name)||print == null){
					name = fileName;
					close();//关闭原来的流
					print = getPrintWriter(name);
				}
			}
		}
	}
	
	
	public Print(String fileName){
		if(print == null){
			synchronized (this){
				if(print == null){
					print = getPrintWriter(fileName);
				}
			}
		}
	}
	public Print(String pageName , String fileName){
		if(print == null){
			synchronized (this){
				if(print == null){
					print = getPrintWriter(pageName , fileName);
				}
			}
		}
	}
	public Print(String pageName , String fileName , boolean boo){
		if(print == null){
			synchronized (this){
				if(print == null){
					print = getPrintWriter(pageName , fileName , boo);
				}
			}
		}
	}
	public void close(){
		if(print != null){
			print.close();
		}
	}
	public void print(String sWord ){
		pwlogResult(sWord);
	}
	public void println(String sWord ){
		pwlogResultln(sWord);
	}
	private  void pwlogResult(String sWord) {
		
		try {
			print.print(sWord);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	private  void pwlogResultln(String sWord) {
		
		try {
			if (LX.isNotEmpty(print)) {
				print.println(sWord);
				print.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public static PrintWriter getPrintWriter( String fileName ){
		return getPrintWriter(fileName,true);
	}
	public static PrintWriter getPrintWriter( String fileName , boolean boo){
		File file = new File(fileName);
		File fileParent = file.getParentFile();
		if (fileParent != null &&!fileParent.exists()) {
			fileParent.mkdirs();
		}
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,boo), "utf-8"),true);
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		return pw;
	}
	public static PrintWriter getPrintWriter(String pageName,String fileName){
		return getPrintWriter(pageName,fileName,true);
	}
	public static PrintWriter getPrintWriter(String pageName,String fileName,boolean boo){
		String path = System.getProperty("user.dir");
		String pan = path.substring(0, path.indexOf("\\"));
		String s = LX.getDay();
		String []arr = s.split("-");
		fileName = pan + "//"+pageName+"/"+arr[0]+"/"+arr[1]+"/"+LX.getDay()+"/"+fileName;
		return getPrintWriter(fileName,boo);
	}

	
	
}
