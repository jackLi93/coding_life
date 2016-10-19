package com.jack.codelife;
//import some class...
//使用netty写一个异步的ECHO的 Client 和Server服务

//netty实现的echo服务器需要下面这些：
	//-  至少一个服务器的handler：这个组件实现了服务的业务逻辑，决定了连接创建后和接收到信息后服务器如何处理
	//-  Bootstrapping:用于配置服务器的启动代码，最少需要设置服务器绑定的端口，用来监听连接请求
@Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdater
{
	@Override
	public void channelRead(ChannelHandlerContext ctx,Object msg){
		ByteBuf in = (ByteBuf)msg;
		System.out.prinln("Server received :"+in.toString(CharsetUtil.UTF_8));
		ctx.write(in);
	}
	@Override
public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
	ctx.writeAndFlush().addListener(ChannelFutureListener.CLOSE);
	}
	@Override
		public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
			cause.printStackTrace();
			ctx.close();
		}
}


======注释：===
1. 以上是Handler的类，用于处理服务器的业务逻辑
2.下面是服务器Server类。

===EchoServer===

public class EchoServer {

    private final int port;
        
    public EchoServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();    
        try {
            ServerBootstrap b = new ServerBootstrap();                            #1
            b.group(group)                                                        #2
             .channel(NioServerSocketChannel.class)                               #2
             .localAddress(new InetSocketAddress(port))                           #2                           
             .childHandler(new ChannelInitializer<SocketChannel>() {              #3
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(
                             new EchoServerHandler());                            #4
                 }
             });

            ChannelFuture f = b.bind().sync();                                    #5
            System.out.println(EchoServer.class.getName() +                       #6
                    " started and listen on " + f.channel().localAddress());      #7
            f.channel().closeFuture().sync();                                     #8
        } finally {                                                               #9
            group.shutdownGracefully().sync();                                    #10
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + EchoServer.class.getSimpleName() +
                    " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }
}

=======以上就是一个简单的ECHO的服务端的处理，实际业务需求的Handler可能更复杂一些==================

Client：

1. client的Handler：
	
	@Sharable                                                            #1
public class EchoClientHandler extends
        SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", 
                        CharsetUtil.UTF_8));                        #2
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
        ByteBuf in) {
        System.out.println("Client received: " + ByteBufUtil
                .hexDump(in.readBytes(in.readableBytes())));        #3
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,          #4
        Throwable cause) {
        cause.printStackTrace();                                    
        ctx.close();                                                
    }
}

=========
Client :

public class EchoClient {

    private final String host;
    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();                               #1 
            b.group(group)                                               #2
             .channel(NioSocketChannel.class)                            #3
             .remoteAddress(new InetSocketAddress(host, port))           #4
             .handler(new ChannelInitializer<SocketChannel>() {          #5
                 @Override
                 public void initChannel(SocketChannel ch) 
                     throws Exception {
                     ch.pipeline().addLast(
                             new EchoClientHandler());                   #6
                 }
             });

            ChannelFuture f = b.connect().sync();                        #7

            f.channel().closeFuture().sync();                            #8
        } finally {
            group.shutdownGracefully().sync();                           #9
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + EchoClient.class.getSimpleName() +
                    " <host> <port>");
            return;
        }

        final String host = args[0];
        final int port = Integer.parseInt(args[1]);

        new EchoClient(host, port).start();
    }
}

================================================================================
小结：
	1.一句话，业务逻辑经常存活于一个或者多个ChannelInboundHanlder；
	2.Netty应用程序通过设置引导bootstrap来初始化客户端(Bootstrap)或者服务端(ServerBootstrap)

参考文档：
	1.《Netty in Action（第二章）》
	2.网络博客：http://ykgarfield.github.io/Netty_in_Action_v5_MEAP/%E7%AC%AC2%E7%AB%A0%20%E4%BD%A0%E7%9A%84%E7%AC%AC%E4%B8%80%E4%B8%AANetty%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F.html