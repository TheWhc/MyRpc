package com.whc.rpc.netty.client;

import com.whc.rpc.RpcClient;
import com.whc.rpc.codec.CommonDecoder;
import com.whc.rpc.codec.CommonEncoder;
import com.whc.rpc.codec.Spliter;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.serializer.CommonSerializer;
import com.whc.rpc.util.RpcMessageChecker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NIO方式消费者客户端类
 * @ClassName: NettyClient
 * @Author: whc
 * @Date: 2021/05/29/23:07
 */
public class NettyClient implements RpcClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
	private static final Bootstrap bootstrap;

	private CommonSerializer serializer;

	static {
		EventLoopGroup group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 1. 指定线程模型
		bootstrap.group(group)
				// 2. 指定IO类型为NIO
				.channel(NioSocketChannel.class)
				// 开启TCP底层心跳机制
				.option(ChannelOption.SO_KEEPALIVE, true);
	}

	private String host;
	private int port;

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public Object sendRequest(RpcRequest rpcRequest) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				// 执行链: head -> CommonEncoder(out) -> Spliter -> CommonDecoder -> NettyClientHandler -> tail
				// out出栈主要是对写回结果进行加工
				// in入栈主要是用来读取服务端数据,写回结果
				// 发送RpcRequest请求对象,经过CommonEncoder编码按照自定义协议编码成ByteBuf对象
				pipeline.addLast(new CommonEncoder(serializer))
						// 接收服务端响应回来的RpcResponse对象,经过Spliter,对网络数据包按照基于固定长度域的拆包器进行拆包
						.addLast(new Spliter())
						// 对数据包按照自定义协议进行解码成POJO对象
						.addLast(new CommonDecoder())
						// 客户端对解码出来的POJO对象进行调用处理
						.addLast(new NettyClientHandler());
			}
		});
		try {
			ChannelFuture future = bootstrap.connect(host, port).sync();
			logger.info("客户端连接到服务器 {}:{}", host, port);
			Channel channel = future.channel();
			if(channel != null) {
				// 发送数据
				channel.writeAndFlush(rpcRequest).addListener(future1 -> {
					if(future1.isSuccess()) {
						logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
					} else {
						logger.error("发送消息时有错误发生: ", future1.cause());
					}
				});
				// 为了让netty不会关闭
				channel.closeFuture().sync();
				// 通过给channel设计别名，获取特定名字下的channel中的内容（这个在hanlder中设置）
				// AttributeKey是，线程隔离的，不会有线程安全问题。
				AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse" + rpcRequest.getRequestId());
				RpcResponse rpcResponse = channel.attr(key).get();
				RpcMessageChecker.check(rpcRequest, rpcResponse);
				return rpcResponse.getData();
			}
		} catch (InterruptedException e) {
			logger.error("发送消息时有错误发生: ", e);
		}
		return null;
	}

	@Override
	public void setSerializer(CommonSerializer serializer) {
		this.serializer = serializer;
	}
}
