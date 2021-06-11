package com.whc.test;

import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.UserService;
import com.whc.rpc.transport.netty.server.NettyServer;
import com.whc.rpc.serializer.CommonSerializer;

/**
 * 测试用Netty服务提供者
 * @ClassName: NettyTestServer
 * @Author: whc
 * @Date: 2021/05/30/0:17
 */
public class NettyTestServer {

	public static void main(String[] args){
		UserService userService = new UserServiceImpl();
		BlogService blogService = new BlogServiceImpl();
		// 服务端需要把自己的ip，端口给注册中心
		NettyServer server = new NettyServer("127.0.0.1", 9000, CommonSerializer.PROTOBUF_SERIALIZER);
		server.publishService(userService, UserService.class);
		server.publishService(blogService, BlogService.class);

		server.start();
	}
}
