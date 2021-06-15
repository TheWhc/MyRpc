package com.whc.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 * 查询: 根据服务名查找地址
 * @ClassName: ServiceDiscovery
 * @Author: whc
 * @Date: 2021/06/13/23:52
 */
public interface ServiceDiscovery {

	/**
	 * 根据服务名称查找服务实体
	 * @param serviceName 服务名称
	 * @return 服务实体
	 */
	InetSocketAddress serviceDiscovery(String serviceName);
}
