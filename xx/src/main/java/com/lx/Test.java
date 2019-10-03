package com.lx;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by 游林夕 on 2019/8/26.
 */
public class Test {
    public static void main(String [] args) throws IOException {
        BufferedImage img = ImageIO.read(new File("D://zw.jpg"));//读取图片
        char [] arr  ="$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/|()1{}[]?-_+~<>i!lI;:,^`'. ".toCharArray();//转换的文字
        int w = img.getWidth();
        int h = img.getHeight();
        int[] data = img.getRGB(0, 0, w, h, null, 0, w);
        for (int y = 0; y < h; y+=4) {
            for (int x = 0; x < w; x+=2) {
                int c = data[x + y * w];
                int R = (c >> 16) & 0xFF;
                int G = (c >> 8) & 0xFF;
                int B = (c >> 0) & 0xFF;
                int a = (int) (0.3f * R + 0.59f * G + 0.11f * B); //to gray
                System.out.print(arr[a%arr.length]+"");
            }
            System.out.println();
        }
    }

}
