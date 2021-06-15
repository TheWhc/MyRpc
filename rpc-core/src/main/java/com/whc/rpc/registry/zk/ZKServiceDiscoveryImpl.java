package com.whc.rpc.registry.zk;

import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.loadbalance.LoadBalancer;
import com.whc.rpc.loadbalance.loadbalancer.RandomLoadBalance;
import com.whc.rpc.registry.ServiceDiscovery;
import com.whc.rpc.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现实现类
 * @ClassName: ZKServiceDiscoveryImpl
 * @Author: whc
 * @Date: 2021/06/14/0:57
 */
public class ZKServiceDiscoveryImpl implements ServiceDiscovery {

	private static final Logger logger = LoggerFactory.getLogger(ZKServiceDiscoveryImpl.class);

	private final LoadBalancer loadBalancer;

	public ZKServiceDiscoveryImpl() {
		this(null);
	}

	public  ZKServiceDiscoveryImpl(LoadBalancer loadBalancer) {
		if(loadBalancer == null) {
			this.loadBalancer = new RandomLoadBalance();
		} else {
			this.loadBalancer = loadBalancer;
		}
	}

	@Override
	public InetSocketAddress serviceDiscovery(String serviceName) {
		CuratorFramework zkClient = CuratorUtils.getZkClient();
		// 获取服务地址列表
		List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, serviceName);
		if (serviceUrlList == null || serviceUrlList.size() == 0) {
			throw new RpcException(RpcError.SERVICE_NOT_FOUND, serviceName);
		}

		// 负载均衡
		String targetServiceUrl = loadBalancer.balance(serviceUrlList);
		logger.info("通过负载均衡策略,获取到服务地址:[{}]", targetServiceUrl);
		String[] socketAddressArray = targetServiceUrl.split(":");
		String host = socketAddressArray[0];
		int port = Integer.parseInt(socketAddressArray[1]);
		return new InetSocketAddress(host, port);
	}
}
