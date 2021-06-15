package com.whc.rpc.handler;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.ResponseCode;
import com.whc.rpc.provider.ServiceProvider;
import com.whc.rpc.provider.ServiceProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 进行过程调用的处理器
 * @ClassName: RequestHandler
 * @Author: whc
 * @Date: 2021/05/24/20:27
 */
public class RequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private static final ServiceProvider serviceProvider;

	static {
		serviceProvider = new ServiceProviderImpl();
	}

	public Object handle(RpcRequest rpcRequest) {
		Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
		return invokeTargetMethod(rpcRequest, service);
	}

	private Object invokeTargetMethod(RpcRequest rpcRequest, Object service){
		Object result;
		try {
			Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
			result = method.invoke(service, rpcRequest.getParameters());
			logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e ) {
			return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
		}
		return result;
	}

}

