package com.whc.test;

import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.UserService;
import com.whc.rpc.registry.DefaultServiceRegistry;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.server.RpcServer;


/**
 * 测试用服务提供方（服务端）
 * @ClassName: TestServer
 * @Author: whc
 * @Date: 2021/05/24/20:48
 */
public class TestServer {

	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		BlogService blogService = new BlogServiceImpl();
		ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
		serviceRegistry.register(userService);
		serviceRegistry.register(blogService);
		RpcServer rpcServer = new RpcServer(serviceRegistry);
		rpcServer.start(9000);
	}

}
