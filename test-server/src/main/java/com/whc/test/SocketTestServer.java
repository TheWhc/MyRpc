package com.whc.test;

import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.UserService;
import com.whc.rpc.provider.ServiceProviderImpl;
import com.whc.rpc.provider.ServiceProvider;
import com.whc.rpc.serializer.CommonSerializer;
import com.whc.rpc.serializer.HessianSerializer;
import com.whc.rpc.transport.socket.server.SocketServer;


/**
 * 测试用服务提供方（服务端）
 * @ClassName: SocketTestServer
 * @Author: whc
 * @Date: 2021/05/24/20:48
 */
public class SocketTestServer {

	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		BlogService blogService = new BlogServiceImpl();
		// 服务端需要把自己的ip，端口给注册中心
		SocketServer socketServer = new SocketServer("127.0.0.1", 9001, CommonSerializer.HESSIAN_SERIALIZER);
		socketServer.publishService(userService, UserService.class);
		socketServer.publishService(blogService, BlogService.class);

		socketServer.start();
	}

}
