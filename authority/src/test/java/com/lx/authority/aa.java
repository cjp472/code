package com.lx.authority;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by 游林夕 on 2019/8/26.
 */
public class aa {
    final static String regularString="@#$&%*!^;, ";
    public static void convertImageToASSIC(BufferedImage bufferedImage){
        int imageWidth=bufferedImage.getWidth();
        int imageHeight=bufferedImage.getHeight();
        for(int coordinateX=0;coordinateX<imageHeight;coordinateX+=150){
            for(int coordinateY=0;coordinateY<imageWidth;coordinateY+=60){
                int orientRGB=bufferedImage.getRGB(coordinateY,coordinateX);
                int componentR=(orientRGB>>16)&0xff;
                int componentG=(orientRGB>>8)&0xff;
                int componentB=orientRGB&0xff;
                int pixelDegree= (int) (componentR*0.3+componentG*0.59+componentB*0.11);
                System.out.print(regularString.charAt(pixelDegree/24));
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
//        File imageFile=new File("D://zw.jpg");
//        System.out.println(imageFile.getAbsoluteFile());
        try {
//            BufferedImage bufferedImage= ImageIO.read(imageFile);
            convertImageToASSIC(getS("游林夕"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static BufferedImage getS(String code){
        int font=2000;
        int width = code.length()*(font+30);
        int height = font+10;
        BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = bi.createGraphics();
        g.setBackground(new Color(255,255,255));
        g.clearRect(0, 0, width, height);
        g.setFont(new Font("微软雅黑,Arial",Font.BOLD,font));
        g.setPaint(new Color(0, 0, 0, 64));//阴影颜色
        g.drawString(code,5,1800);//先绘制阴影
        g.setColor(Color.black);
        g.drawString(code,5,1800);//上下
        return bi;
    }

}
