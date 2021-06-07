package com.whc.rpc.netty.server;

import com.whc.rpc.RpcServer;
import com.whc.rpc.codec.CommonDecoder;
import com.whc.rpc.codec.CommonEncoder;
import com.whc.rpc.codec.Spliter;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NIO方式服务提供者
 * @ClassName: NettyServer
 * @Author: whc
 * @Date: 2021/05/29/20:44
 */
public class NettyServer implements RpcServer {

	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private CommonSerializer serializer;

	@Override
	public void start(int port) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}

		// bossGroup 表示监听端口,accept新的连接请求
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// workerGroup表示处理每一条连接的数据读写的线程组
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			// 引导类,引导进行服务端的启动工作
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			// 配置两大线程组
			serverBootstrap.group(bossGroup, workerGroup)
					// 指定服务端IO模型为NIO
					.channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					// 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,
					// 如果连接建立频繁,服务器处理创建新连接较慢,可以适当调大这个参数
					.option(ChannelOption.SO_BACKLOG, 256)
					// 开启TCP底层心跳机制
					.option(ChannelOption.SO_KEEPALIVE, true)
					// 开启Nagle算法,true表示关闭
					// 如果要求高实时性,有数据发送时就马上发送,就关闭
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// 返回和这条连接相关的逻辑处理链,采用了责任链模式
							// 执行链: head -> CommonEncoder(out) -> Spliter -> CommonDecoder -> NettyServerHandler -> tail
							// out出栈主要是对写回结果进行加工
							// in入栈主要是用来读取客户端数据,写回结果

							// 如果添加执行链的顺序为: head -> Spliter -> CommonDecoder -> NettyServerHandler -> CommonEncoder(out) -> tail
							// 那么当写回数据出栈的时候,即到达了NettyServerHandler时进行ctx.writeAndFlush时,是从当前处理器从后往前找,找不到会报错
							ChannelPipeline pipeline = ch.pipeline();
							// 支持自定义序列化方式
							// 服务端发回响应对象RpcResponse,经过编码器转化为ByteBuf对象
							pipeline.addLast(new CommonEncoder(serializer));
							// 基于长度域拆包器以及拒绝非本协议连接
							// 对客户端传送过来的数据包进行拆包,然后拼装成符合自定义数据包大小的ByteBuf
							pipeline.addLast(new Spliter());
							// 对ByteBuf解码成POJO对象(对应RpcRequest对象)
							pipeline.addLast(new CommonDecoder());
							// 对RpcRequest对象进行解析处理
							pipeline.addLast(new NettyServerHandler());

						}
					});
			// 实现端口绑定,建立连接, 会让主线程间接调用wait()方法，进而实现阻塞的效果
			// 保证在初始化完成后才进行操作，避免调用一个初始化未完成的句柄
			ChannelFuture future = serverBootstrap.bind(port).sync();
			// 为了让netty不会关闭
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			logger.error("启动服务器时有错误发生: ", e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	@Override
	public void setSerializer(CommonSerializer serializer) {
		this.serializer = serializer;
	}

}
