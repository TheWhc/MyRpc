package com.whc.test;

import com.whc.rpc.RpcClient;
import com.whc.rpc.RpcClientProxy;
import com.whc.rpc.api.Blog;
import com.whc.rpc.api.BlogService;
import com.whc.rpc.api.User;
import com.whc.rpc.api.UserService;
import com.whc.rpc.netty.client.NettyClient;
import com.whc.rpc.serializer.HessianSerializer;

/**
 * @ClassName: NettyTestClient
 * @Author: whc
 * @Date: 2021/05/30/0:15
 */
public class NettyTestClient {

	public static void main(String[] args) {
		RpcClient client = new NettyClient("127.0.0.1", 9000);
		client.setSerializer(new HessianSerializer());
		RpcClientProxy proxy = new RpcClientProxy(client);
		// 不同的service需要进行不同的封装,客户端只知道service接口,需要一层动态代理根据反射封装不同的Service
		UserService userService = proxy.getProxy(UserService.class);
		// 服务方法1
		User userByUserId = userService.getUserByUserId(10);
		System.out.println(userByUserId);
		// 服务方法2
		User user = User.builder().userName("张三").id(100).sex(true).build();
		Integer integer = userService.insertUserId(user);
		System.out.println("向服务端插入数据:" + integer);
		BlogService blogService = proxy.getProxy(BlogService.class);
		Blog blog = blogService.getBlogById(1);
		System.out.println("获取博客:" + blog);
	}
}
