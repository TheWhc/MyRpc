package com.whc.test;

import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.UserService;
import com.whc.rpc.netty.server.NettyServer;
import com.whc.rpc.registry.DefaultServiceRegistry;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.serializer.HessianSerializer;
import com.whc.rpc.socket.server.SocketServer;

/**
 * 测试用Netty服务提供者
 * @ClassName: NettyTestServer
 * @Author: whc
 * @Date: 2021/05/30/0:17
 */
public class NettyTestServer {

	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		BlogService blogService = new BlogServiceImpl();
		ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
		serviceRegistry.register(userService);
		serviceRegistry.register(blogService);
		NettyServer socketServer = new NettyServer();
		socketServer.setSerializer(new HessianSerializer());
		socketServer.start(9000);
	}
}
