package com.whc.rpc.provider;

import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的服务注册表,保存服务端本地服务
 * @ClassName: ServiceProviderImpl
 * @Author: whc
 * @Date: 2021/05/24/20:17
 */
public class ServiceProviderImpl implements ServiceProvider {

	private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

	// 缓存到本地的server服务
	private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();
	private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

	@Override
	public <T> void addServiceProvider(T service, Class<T> serviceClass) {
		String serviceName = serviceClass.getCanonicalName();
		if(registeredService.contains(serviceName)) return;
		registeredService.add(serviceName);
		// com.whc.test.UserService -> com.whc.test.UserServiceImpl
		serviceMap.put(serviceName, service);
		logger.info("向接口: {} 注册服务: {}", service.getClass().getInterfaces(), serviceName);
	}

	@Override
	public Object getServiceProvider(String serviceName) {
		// com.whc.test.UserService -> com.whc.test.UserServiceImpl
		Object service = serviceMap.get(serviceName);
		if(service == null) {
			throw new RpcException(RpcError.SERVICE_NOT_FOUND);
		}
		return service;
	}
}
