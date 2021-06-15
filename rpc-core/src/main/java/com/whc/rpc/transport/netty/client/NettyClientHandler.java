package com.whc.rpc.transport.netty.client;

import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.factory.SingletonFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty客户端处理器
 * @ClassName: NettyClientHandler
 * @Author: whc
 * @Date: 2021/05/30/0:06
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

	private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

	private final UnprocessedRequests unprocessedRequests;

	public NettyClientHandler() {
		this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
		try {
			logger.info(String.format("客户端接收到消息: %s", msg));
			unprocessedRequests.complete(msg);
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("过程调用时有错误发生:");
		cause.printStackTrace();
		ctx.close();
	}
}
