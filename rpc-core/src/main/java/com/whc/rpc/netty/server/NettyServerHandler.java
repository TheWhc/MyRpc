package com.whc.rpc.netty.server;

import com.whc.rpc.RequestHandler;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.registry.DefaultServiceRegistry;
import com.whc.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty中处理RpcRequest的Handler
 * @ClassName: NettyServerHandler
 * @Author: whc
 * @Date: 2021/05/29/21:49
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
	private static RequestHandler requestHandler;
	private static ServiceRegistry serviceRegistry;

	static {
		requestHandler = new RequestHandler();
		serviceRegistry = new DefaultServiceRegistry();
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		try {
			logger.info("服务器接收到消息:{}", msg);
			String interfaceName = msg.getInterfaceName();
			Object server = serviceRegistry.getService(interfaceName);
			Object result = requestHandler.handle(msg, server);
			// 向客户端返回响应数据
			// 注意服务端处理器这里的执行链顺序,因为是ctx.writeAndFlush而不是ch.writeAndFlush,所以在执行出栈(out)时,是从当前ctx处理器从后往前找,而不是从通道最后从后往前找
			ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result, msg.getRequestId()));
			// 消息发送完毕关闭连接
			future.addListener(ChannelFutureListener.CLOSE);
		} finally {
			// 记得释放对象,防止内存泄漏
			ReferenceCountUtil.release(msg);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("处理过程调用时有错误发生");
		cause.printStackTrace();
		ctx.close();
	}

}
