package com.whc.rpc;

import com.whc.rpc.entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC客户端代理类
 * 动态代理封装request对象
 * @ClassName: RpcClientProxy
 * @Author: whc
 * @Date: 2021/05/24/20:11
 */
public class RpcClientProxy implements InvocationHandler {

	private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

	private final RpcClient client;

	public RpcClientProxy(RpcClient client) {
		this.client = client;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> clazz) {
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
	}

	// jdk 动态代理， 每一次代理对象调用方法，会经过此方法增强 (反射获取request对象，socket发送至客户端)
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
		RpcRequest rpcRequest = RpcRequest.builder()
				.requestId(UUID.randomUUID().toString())
				.interfaceName(method.getDeclaringClass().getName())
				.methodName(method.getName())
				.parameters(args)
				.paramTypes(method.getParameterTypes())
				.build();
		return client.sendRequest(rpcRequest);
	}
}
