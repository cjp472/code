package com.lx.api.netty;//说明:

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 创建人:游林夕/2019/4/16 10 55
 */
public class Demo {

}
class DemoServer {

    public static void main(String[] args) throws Exception {
        int port = 8000;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //采用默认值
            }
        }
        new DemoServer().bind(port);
    }

    public void bind(int port) throws Exception {
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();//负责接收客户端连接线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();//负责处理客户端i/o事件、task任务、监听任务组
        try {
            ServerBootstrap b = new ServerBootstrap();//启动 NIO 服务的辅助启动类
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//配置 Channel
                    .option(ChannelOption.SO_BACKLOG, 1024)// 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                    .childHandler(new ServerChannelInitializer())//BACKLOG用于构造服务端套接字ServerSocket对象，
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//是否启用心跳保活机制

            //绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            //等待服务器监听端口关闭
            /*channel.closeFuture().sync()实际是如何工作:
            channel.closeFuture()不做任何操作，只是简单的返回channel对象中的closeFuture对象，对于每个Channel对象，都会有唯一的一个CloseFuture，用来表示关闭的Future，
            所有执行channel.closeFuture().sync()就是执行的CloseFuturn的sync方法，从上面的解释可以知道，这步是会将当前线程阻塞在CloseFuture上*/
            f.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //字符串解码和编码
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        //服务器的逻辑
        pipeline.addLast("handler", new DemoServerHandler());
    }
}
class DemoServerHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Client say : " + msg.toString());
        //返回客户端消息 - 我已经接收到了你的消息
        ctx.writeAndFlush("Received your message : " + msg.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("RemoteAddress : " + ctx.channel().remoteAddress() + " active !");
        ctx.writeAndFlush("连接成功！");
        super.channelActive(ctx);
    }
}

class DemoClient {

    public static String host = "127.0.0.1"; //服务器IP地址
    public static int port = 12345; //服务器端口

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer());

            //连接客户端
            Channel channel = b.connect(host, port).sync().channel();

            //控制台输入
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    continue;
                }
                //向服务端发送数据
                channel.writeAndFlush(line);
            }
        } finally {
            //优雅退出，释放线程池资源
            group.shutdownGracefully();
        }
    }
}
class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //字符串解码和编码
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        //客户端的逻辑
        pipeline.addLast("handler", new DemoClientHandler());
    }
}
class DemoClientHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Server say : " + msg.toString());
    }
}