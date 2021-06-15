package com.whc.rpc.registry.zk;

import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * 服务注册实现类
 * @ClassName: ZKServiceRegistryImpl
 * @Author: whc
 * @Date: 2021/06/13/23:53
 */
public class ZKServiceRegistryImpl implements ServiceRegistry {

	@Override
	public void register(String serviceName, InetSocketAddress inetSocketAddress) {
		String servicePersistentPath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + serviceName;
		String serviceEphemeralPath = servicePersistentPath + inetSocketAddress;
		CuratorFramework zkClient = CuratorUtils.getZkClient();
		// 创建服务名永久节点, 服务地址为临时节点
		CuratorUtils.createPersistentNode(zkClient, servicePersistentPath);
		CuratorUtils.createEphemeralNode(zkClient, serviceEphemeralPath);
	}

}
