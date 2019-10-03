package com.lx.api.netty;//说明:


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.regex.Pattern;

/**[2019/4/16]说明:
   __ _ _  __    ___   __    __   __ _ _
  / / / /  \#\  /#/#|  \#\  /#/   \ \ \ \
 / / / /    \#--#/|#|   \#\/#/     \ \ \ \
 \ \ \ \     [##] |#|___/#/\#\     / / / /
  \_\_\_\    [##] |####/#/  \#\   /_/_/*/
public class MyDemo {
    public static void main(String [] args) throws Exception {
        String ip = NetUtils.getLocalAddress().getHostAddress();
//        bind(ip,12345);
        call(ip,12345);
    }
    //说明:
    /**{ ylx } 2019/4/16 16:01 */
    public static void bind(String host , int port) throws InterruptedException {
        EventLoopGroup accept = new NioEventLoopGroup();//接受请求的工作组
        EventLoopGroup work = new NioEventLoopGroup();//执行任务的工作组
        try{
            new ServerBootstrap()
                .group(accept,work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)// 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                .childHandler(new ChannelInitializer<SocketChannel>(){//BACKLOG用于构造服务端套接字ServerSocket对象，
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                            .addLast(new ObjectEncoder())
                            .addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)))
                            .addLast("handler", new SimpleChannelInboundHandler(){//处理消息
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("客户端: " + msg);
                                    ctx.writeAndFlush(msg);//返回
                                }
                            });
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true)//是否启用心跳保活机制
                .bind(port).sync().channel().closeFuture().sync();//等待链接
        } finally {
            accept.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

    public static void call(String ip, int port) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup worker = new NioEventLoopGroup();
        try{
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            System.out.println("active");
                            super.channelActive(ctx);
                            //发送消息给服务端
                            ctx.writeAndFlush(new HashMap());
                        }
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                            System.out.println("Server say : " + o);
                        }
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            System.out.println(msg);
                            ctx.close();
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            channelFuture.channel().closeFuture().sync();
        }finally {
            worker.shutdownGracefully();
        }
    }

}
class NetUtils {


    private static final Pattern IP_PATTERN       = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    public static final String   ANYHOST          = "0.0.0.0";

    public static final String   LOCALHOST        = "127.0.0.1";

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) return false;
        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }

    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
    }

    public static boolean isAnyHost(String host) {
        return "0.0.0.0".equals(host);
    }

    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     *
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() throws UnknownHostException, SocketException {
        InetAddress localAddress = null;
        localAddress = InetAddress.getLocalHost();
        if (isValidAddress(localAddress)) {
            return localAddress;
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                if (addresses != null) {
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (isValidAddress(address)) {
                            return address;
                        }
                    }
                }
            }
        }
        return localAddress;
    }

}