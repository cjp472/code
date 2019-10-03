package com.lx.util;//说明:

/**
 * 创建人:游林夕/2019/5/30 11 23
 */
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lx.entity.My_user;
import com.lx.entity.TGRespose;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 类名称：QRCodeMax
 * 类描述：生成二维码图片+背景+文字描述工具类
 * 创建人：一个除了帅气，一无是处的男人
 * 创建时间：2018年12月x日x点x分x秒
 * 修改时间：2019年2月x日x点x分x秒
 * 修改备注：更新有参数构造
 * @version： 2.0
 *
 */
public class QRCodeMax {


    //文字显示
    private static final int QRCOLOR = 0x201f1f; // 二维码颜色:黑色
    private static final int BGWHITE = 0xFFFFFF; //二维码背景颜色:白色

    // 设置QR二维码参数信息
    private static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
        private static final long serialVersionUID = 1L;
        {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);// 设置QR二维码的纠错级别(H为最高级别)
            put(EncodeHintType.CHARACTER_SET, "utf-8");// 设置编码方式
            put(EncodeHintType.MARGIN, 0);// 白边
        }
    };

    /**
     * 生成二维码图片+背景+文字描述
     * @param codeFile 生成图地址
     * @param bgImgFile 背景图地址
     * @param WIDTH 二维码宽度
     * @param HEIGHT 二维码高度
     * @param qrUrl 二维码识别地址
     * @param note  文字描述1
     * @param tui   文字描述2
     * @param size 文字大小
     * @param imagesX 二维码x轴方向
     * @param imagesY 二维码y轴方向
     * @param text1X 文字描述1x轴方向
     * @param text1Y 文字描述1y轴方向
     * @param text2X 文字描述2x轴方向
     * @param text2Y 文字描述2y轴方向
     */
    public static void CreatQRCode( File codeFile, File bgImgFile,Integer WIDTH,Integer HEIGHT,String qrUrl,
                                    String note,String tui,Integer size
                                ,Integer imagesX,Integer imagesY,Integer text1X,Integer text1Y
                                ,Integer text2X,Integer text2Y,My_user user) throws Exception {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            // 参数顺序分别为: 编码内容,编码类型,生成图片宽度,生成图片高度,设置参数
            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            // 开始利用二维码数据创建Bitmap图片，分别设为黑(0xFFFFFFFF) 白(0xFF000000)两色
            int w = ((int)Math.sqrt(WIDTH*HEIGHT*2))/2; // 分成六个部分进行绘制
            for (int x = 0; x < WIDTH; x++) {
                int rgb =0;
                for (int y = 0; y < HEIGHT; y++) {
                    int d = (int)Math.sqrt((WIDTH/2-x)*(WIDTH/2-x)+(HEIGHT/2-y)*(HEIGHT/2-y))*255/w; // 使d从0递增到255，实际可能只是接近255
                    rgb=new Color(255-d/5, d, (255-d)/2).getRGB(); // 设置颜色
                    image.setRGB(x, y, bm.get(x, y) ? rgb : BGWHITE);
                }
            }

            /*
             * 	添加背景图片
             */
            BufferedImage backgroundImage = ImageIO.read(bgImgFile);
            int bgWidth=backgroundImage.getWidth();
            int qrWidth=image.getWidth();
            Graphics2D rng=backgroundImage.createGraphics();
            rng.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
            rng.drawImage(image,imagesX,imagesY,WIDTH,HEIGHT,null);

            /*
             * 	文字描述参数设置
             */

            Color textColor=Color.white;
            rng.setColor(textColor);
            rng.drawImage(backgroundImage,0,0,null);
            //设置字体类型和大小(BOLD加粗/ PLAIN平常)
            rng.setFont(new Font("微软雅黑,Arial",Font.BOLD,size));

            int strWidth=rng.getFontMetrics().stringWidth(note);

            rng.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//设置抗锯齿

            //g2d.setPaint(Color.BLACK);//正文颜色
            //g2d.drawString(txt, x, y);//用正文颜色覆盖上去

            //文字1显示位置
            rng.setPaint(new Color(0, 0, 0, 64));//阴影颜色
            rng.drawString(note,text1X,text1Y);//先绘制阴影
            rng.setColor(Color.black);
            rng.drawString(note,text1X,text1Y);//上下

            rng.setPaint(new Color(0, 0, 0, 64));//阴影颜色
            rng.drawString("长按自动识别图中二维码!",90,670);//先绘制阴影
            rng.setColor(Color.black);
            rng.drawString("长按自动识别图中二维码!",90,670);//上下



            //文字2显示位置
            rng.setFont(new Font("微软雅黑,Arial",Font.BOLD,25));
            String [] arr = tui.split("\n");
            for (int i = 0 ;i<arr.length;i++){
                rng.setPaint(new Color(0, 0, 0, 64));//阴影颜色
                rng.drawString(arr[i],text2X,text2Y+i*(27));//先绘制阴影
                rng.setColor(Color.black);
                rng.drawString(arr[i],text2X,text2Y+i*(27));
            }
            String grxx = "      ◇ ◇ ◇个 人 信 息◇ ◇ ◇\n" +
                    LX.left("● 余额",15)+"● 推荐奖励\n" +
                    LX.left("已提现金额:" + user.getYtxje().setScale(2, BigDecimal.ROUND_HALF_DOWN) + "元;",13)+ "可提现金额:" + user.getWtxje().setScale(2,BigDecimal.ROUND_HALF_DOWN) + "元;\n" +
                    LX.left("未结算金额:" + user.getWjsje().setScale(2,BigDecimal.ROUND_HALF_DOWN) + "元;",13)+ "已推广奖励:" + user.getTgje().setScale(2,BigDecimal.ROUND_HALF_DOWN) + "元;\n" +
                    "       输入'教学'可以查询帮助信息\n" +
                    "温馨提示:付款和收货第二天会收到消息!";
            //文字2显示位置
            text2Y = 740;
            text2X =90;
            rng.setFont(new Font("微软雅黑,Arial",Font.PLAIN,20));
            arr = grxx.split("\n");
            for (int i = 0 ;i<arr.length;i++){
                rng.setPaint(new Color(0, 0, 0, 64));//阴影颜色
                rng.drawString(arr[i],text2X,text2Y+i*(24));//先绘制阴影
                rng.setColor(Color.black);
                rng.drawString(arr[i],text2X,text2Y+i*(24));
            }


            rng.dispose();
            image=backgroundImage;
            image.flush();
            ImageIO.write(image, "jpg", codeFile);
    }

    public static String crerteQr(TGRespose tg, My_user user) throws Exception {
        File bgImgFile=new File(System.getProperty("user.dir")+"\\wx\\ewm.jpg");//背景图片
        String path = System.getProperty("user.dir")+"\\wx\\"+tg.getUser_id()+".jpg";
        File QrCodeFile = new File(path);//生成图片位置
        CreatQRCode(QrCodeFile,bgImgFile, 148, 148, "http://52ylx.cn:8889/copy.html?tkl="+tg.getUrl()+"&img="+ URLEncoder.encode(tg.getImgUrl()), tg.getTitle().substring(0,10)+"...",tg.getText()
                , 25, 172, 462, 120, 50, 120, 330,user);
        return path;
    }
    //说明:提现信息
    /**{ ylx } 2019/6/8 12:49 */
    public static String creatQTX(Map<String,String> map) throws Exception {
        String path = System.getProperty("user.dir")+"\\wx\\"+map.get("user_id")+".jpg";
        File QrCodeFile = new File(path);//生成图片位置
        File bgImgFile=new File(System.getProperty("user.dir")+"\\wx\\cxt.jpg");//背景图片
        BufferedImage backgroundImage = ImageIO.read(bgImgFile);
        Graphics2D rng=backgroundImage.createGraphics();
        //设置字体类型和大小(BOLD加粗/ PLAIN平常)
        rng.setFont(new Font("微软雅黑,Arial",Font.BOLD,40));
        rng.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);//设置抗锯齿
        rng.setColor(Color.black);
        rng.drawString("长按自动识别图中二维码!",200,1360);//上下
        String text ="尊敬的用户:"+map.get("name")+"\n" +
                "恭喜您申请提现成功:"+map.get("wtxje")+"元\n" +
                "请长按识别下面二维码,加她为好友!\n" +
                "并将图片发送给她,她会为您提现!\n" +
                "提现时间:"+LX.getTime();
        String [] arr = text.split("\n");
        rng.setFont(new Font("微软雅黑,Arial",Font.BOLD,35));
        for (int i = 0 ;i<arr.length;i++){
            rng.drawString(arr[i],100,80+i*(45));
        }

        rng.dispose();
        backgroundImage.flush();
        ImageIO.write(backgroundImage, "jpg", QrCodeFile);
        return path;
    }
    //说明:订单信息查询
    /**{ ylx } 2019/6/8 15:08 */
    public static String creatDD(Map map) throws Exception {
        String path = System.getProperty("user.dir")+"\\wx\\"+map.get("user_id")+".jpg";
        List<List<List<String>>> allValue = new ArrayList<>();
        List<String[]> headTitles = new ArrayList<>();
        String[] h1 = new String[]{"创建时间","商品信息","状态","返利金额"};
        for (int i=0;i<4;i++){
            headTitles.add(h1);
            allValue.add(new ArrayList<>());
        }
        List<Map<String,String>> ls =(List<Map<String,String>>)map.get("ls");
        for (Map<String,String> m : ls){
            List ll = allValue.get(Integer.parseInt(m.get("lb")));
            List<String> content1 =Arrays.asList(new String[]{m.get("cr_time"),m.get("title").length()>20?m.get("title").substring(0,20)+"...":m.get("title"),m.get("status"),m.get("fx")});
            ll.add(content1);
        }

        List<String> titles = new ArrayList<>();
        titles.add("可提现列表");
        titles.add("未结算列表");
        titles.add("已提现列表");
        titles.add("推广列表");
        graphicsGeneration(allValue,titles,headTitles ,path,4);
        return path;
    }
    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) throws Exception {
//        initChartData();
        crerteQr(new TGRespose("","","","带哦接待偶时间地哦啊惊悚的骄傲","",LX.getBigDecimal(0),LX.getBigDecimal(0)),new My_user("","","","",""));
    }

    public static String graphicsGeneration(List<List<List<String>>> allValue,List<String> titles,List<String[]> headers ,String path,int totalcol) throws Exception {
        int rows = 0;
        for (List<List<String>> typeV : allValue) {
            if (typeV != null && typeV.size() > 0) {
                rows += (2+typeV.size());
            }
        }
        // 实际数据行数+标题+备注
        int totalrow = 1+rows;
        int imageWidth = 950;
        int imageHeight = totalrow * 30 + 20;
        int rowheight = 30;
        int startHeight = 10;
        int startWidth = 10;
        int colwidth = ((imageWidth - 20) / totalcol);

        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imageWidth, imageHeight);
        //画背景
        graphics.setColor(new Color(0, 112, 192));
        int startH = 1;
        for (List<List<String>> typeV : allValue) {
            if (typeV != null && typeV.size() > 0) {
                graphics.fillRect(startWidth+1, startHeight+startH*rowheight+1, imageWidth - startWidth-5-1,rowheight-1);
                startH+=2+typeV.size();
            }
        }

        graphics.setColor(new Color(220, 240, 240));
        // 画横线

        for (int j = 0; j < totalrow - 1; j++) {
            graphics.setColor(Color.black);
            graphics.drawLine(startWidth, startHeight + (j + 1) * rowheight, imageWidth - 5,
                    startHeight + (j + 1) * rowheight);
        }

        // 画竖线
        graphics.setColor(Color.black);
        startH = 1;
        int rightLine = 0 ;
        for (List<List<String>> typeV : allValue) {

            if (typeV != null && typeV.size() > 0) {
                for (int k = 0; k < totalcol+1; k++) {
                    rightLine = getRightMargin(k,startWidth, colwidth,imageWidth);
                    graphics.drawLine(rightLine, startHeight + startH*rowheight, rightLine,
                            startHeight + (typeV.size()+1+startH)*rowheight);
                }
                startH+=2+typeV.size();
            }
        }

        // 设置字体
        Font font = new Font("华文楷体", Font.BOLD, 20);
        graphics.setFont(font);

        // 写标题
        startH = 1;
        int i = 0;
        for (List<List<String>> typeV : allValue) {
            if (typeV != null && typeV.size() > 0) {
                graphics.drawString(titles.get(i), imageWidth / 3 + startWidth+30, startHeight + startH*rowheight - 10);
                startH+=2+typeV.size();
            }
            i++;
        }


        // 写入表头
        graphics.setColor(Color.WHITE);
        font = new Font("华文楷体", Font.BOLD, 20);
        graphics.setFont(font);
        startH = 2;
        i = 0;
        for (List<List<String>> typeV : allValue) {
            if (typeV != null && typeV.size() > 0) {

                String[] headCells = headers.get(i);
                for (int m = 0; m < headCells.length; m++) {
                    rightLine = getRightMargin(m,startWidth, colwidth,imageWidth)+5;
                    graphics.drawString(headCells[m].toString(), rightLine,
                            startHeight + rowheight * startH - 10);
                }
                startH+=2+typeV.size();
            }
            i++;
        }


        // 写入内容
        graphics.setColor(Color.black);
        font = new Font("华文楷体", Font.PLAIN, 20);
        graphics.setFont(font);
        startH = 3;
        i = 0;
        for (List<List<String>> typeV : allValue) {
            if (typeV != null && typeV.size() > 0) {
                for (int n = 0; n < typeV.size(); n++) {
                    List<String> arr = typeV.get(n);
                    for (int l = 0; l < arr.size(); l++) {
                        rightLine = getRightMargin(l,startWidth, colwidth,imageWidth)+5;
                        graphics.drawString(arr.get(l).toString(), rightLine,
                                startHeight + rowheight * (n + startH) - 10);
                    }
                }
                startH+=2+typeV.size();
            }
            i++;
        }
        ImageIO.write(image, "jpg", new File(path));
        return path;
    }

    /**
     * 获取竖线和文字的水平位置
     * @param k
     * @param startWidth
     * @param colwidth
     * @param imageWidth
     * @return
     */
    private static int getRightMargin(int k, int startWidth, int colwidth, int imageWidth) {
        int rightLine = 0;
        if (k == 0) {
            rightLine = startWidth;
        } else if (k == 1) {
            rightLine = startWidth + 2 * colwidth / 2;
        } else if (k == 2) {
            rightLine = startWidth + 11 * colwidth / 4;
        } else if (k == 3) {
            rightLine = startWidth + 7 * colwidth / 2;
        } else if (k == 4) {
            rightLine = imageWidth - 5;
        }
        return rightLine;
    }
}

