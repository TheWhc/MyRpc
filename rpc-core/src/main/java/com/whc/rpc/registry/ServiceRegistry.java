package com.whc.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 * 注册:保存服务和地址
 * @ClassName: ServiceRegistry
 * @Author: whc
 * @Date: 2021/06/09/22:29
 */
public interface ServiceRegistry {

	/**
	 * 将一个服务注册进注册表
	 * @param serviceName 服务名称
	 * @param inetSocketAddress 提供服务的地址
	 */
	void register(String serviceName, InetSocketAddress inetSocketAddress);

}
