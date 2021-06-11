package com.whc.rpc.transport.netty.client;

import com.whc.rpc.transport.RpcClient;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.factory.SingletonFactory;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.registry.ZkServiceRegistry;
import com.whc.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * NIO方式消费者客户端类
 * @ClassName: NettyClient
 * @Author: whc
 * @Date: 2021/05/29/23:07
 */
public class NettyClient implements RpcClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
	private static final EventLoopGroup group;
	private static final Bootstrap bootstrap;

	static {
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		// 1. 指定线程模型
		bootstrap.group(group)
				// 2. 指定IO类型为NIO
				.channel(NioSocketChannel.class);
	}

	private final ServiceRegistry serviceDiscovery;
	private final CommonSerializer serializer;
	private final UnprocessedRequests unprocessedRequests;

	public NettyClient() {
		this(DEFAULT_SERIALIZER);
	}

	public NettyClient(Integer serializer) {
		// 初始化注册中心，建立连接
		this.serviceDiscovery = new ZkServiceRegistry();
		this.serializer = CommonSerializer.getByCode(serializer);
		this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
	}

	@Override
	public CompletableFuture<RpcResponse> sendRequest(RpcRequest rpcRequest) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}

		CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();

		try {
			InetSocketAddress inetSocketAddress = serviceDiscovery.serviceDiscovery(rpcRequest.getInterfaceName());
			Channel channel = ChannelProvider.get(inetSocketAddress, serializer);

			if (!channel.isActive()) {
				group.shutdownGracefully();
				return null;
			}

			unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);

			channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future1 -> {
				if (future1.isSuccess()) {
					logger.info(String.format("客户端发送消息: %s", rpcRequest.toString()));
				} else {
					// 为了让netty不会关闭
					future1.channel().close();
					resultFuture.completeExceptionally(future1.cause());
					logger.error("发送消息时有错误发生: ", future1.cause());
				}
			});
		} catch (InterruptedException e) {
			unprocessedRequests.remove(rpcRequest.getRequestId());
			logger.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}

		return resultFuture;
	}

}
