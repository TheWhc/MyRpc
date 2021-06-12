package com.whc.test;

import com.whc.rpc.loadbalancer.RoundLoadBalance;
import com.whc.rpc.transport.RpcClient;
import com.whc.rpc.transport.RpcClientProxy;
import com.whc.rpc.api.Blog;
import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.User;
import com.whc.rpc.api.UserService;
import com.whc.rpc.transport.netty.client.NettyClient;
import com.whc.rpc.serializer.CommonSerializer;

/**
 * @ClassName: NettyTestClient
 * @Author: whc
 * @Date: 2021/05/30/0:15
 */
public class NettyTestClient {

	public static void main(String[] args) {
//		RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER);

		// 传入轮询负载均衡
		RpcClient client = new NettyClient(CommonSerializer.PROTOBUF_SERIALIZER, new RoundLoadBalance());

		RpcClientProxy proxy = new RpcClientProxy(client);
		// 不同的service需要进行不同的封装,客户端只知道service接口,需要一层动态代理根据反射封装不同的Service
		UserService userService = proxy.getProxy(UserService.class);

		// 服务方法
		User userByUserId = userService.getUserByUserId(10);
		System.out.println(userByUserId);

		// 服务方法
		User user = User.builder().userName("张三").id(100).sex(true).build();
		Integer integer = userService.insertUserId(user);
		System.out.println("向服务端插入数据:" + integer);

		// 服务方法
		BlogService blogService = proxy.getProxy(BlogService.class);
		Blog blog = blogService.getBlogById(1);
		System.out.println("获取博客:" + blog);
	}
}
