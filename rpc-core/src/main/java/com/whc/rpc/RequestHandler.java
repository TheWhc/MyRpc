package com.whc.rpc;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.ResponseCode;
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

	public Object handle(RpcRequest rpcRequest, Object service) {
		Object result = null;
		try {
			result = invokeTargetMethod(rpcRequest, service);
			logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("调用或发送时有错误发生：", e);
		} return result;
	}

	private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws IllegalAccessException, InvocationTargetException {
		Method method;
		try {
			method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
		} catch (NoSuchMethodException e) {
			return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequest.getRequestId());
		}
		return method.invoke(service, rpcRequest.getParameters());
	}

}

