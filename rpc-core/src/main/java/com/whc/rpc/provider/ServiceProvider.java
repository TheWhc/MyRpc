package com.whc.rpc.provider;

/**
 * 保存和提供服务实例对象
 * @ClassName: ServiceProvider
 * @Author: whc
 * @Date: 2021/05/24/20:15
 */
public interface ServiceProvider {

	<T> void addServiceProvider(T service, Class<T> serviceClass);

	Object getServiceProvider(String serviceName);
}
