package com.whc.rpc.transport.netty.client;

import com.whc.rpc.codec.CommonDecoder;
import com.whc.rpc.codec.CommonEncoder;
import com.whc.rpc.codec.Spliter;
import com.whc.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 用于获取 Channel 对象
 * @ClassName: ChannelProvider
 * @Author: whc
 * @Date: 2021/06/10/0:55
 */
public class ChannelProvider {

	private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);
	private static EventLoopGroup eventLoopGroup;
	private static Bootstrap bootstrap = initializeBootstrap();

	private static Map<String, Channel> channels = new ConcurrentHashMap<>();

	public static Channel get(InetSocketAddress inetSocketAddress, CommonSerializer serializer) throws InterruptedException {
		String key = inetSocketAddress.toString() + serializer.getCode();
		if (channels.containsKey(key)) {
			Channel channel = channels.get(key);
			if(channels != null && channel.isActive()) {
				return channel;
			} else {
				channels.remove(key);
			}
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

		Channel channel = null;
		try {
			channel = connect(bootstrap, inetSocketAddress);
		} catch (ExecutionException e) {
			logger.error("连接客户端时有错误发生", e);
			return null;
		}
		channels.put(key, channel);
		return channel;
	}

	private static Channel connect(Bootstrap bootstrap, InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
		CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
		bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
			if (future.isSuccess()) {
				logger.info("客户端连接成功!");
				completableFuture.complete(future.channel());
			} else {
				throw new IllegalStateException();
			}
		});
		return completableFuture.get();
	}

	private static Bootstrap initializeBootstrap() {
		eventLoopGroup = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup)
				.channel(NioSocketChannel.class)
				//连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
				//是否开启 TCP 底层心跳机制
				.option(ChannelOption.SO_KEEPALIVE, true)
				//TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
				.option(ChannelOption.TCP_NODELAY, true);
		return bootstrap;
	}
}
