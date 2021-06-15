package com.whc.rpc.registry;

import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.loadbalance.LoadBalancer;
import com.whc.rpc.loadbalance.loadbalancer.RandomLoadBalance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * ZooKeeper服务注册中心
 * @ClassName: ZkServiceRegistry
 * @Author: whc
 * @Date: 2021/06/09/22:34
 */
public class ZkServiceRegistry {

	/*private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

	// curator提供的zookeeper客户端
	private CuratorFramework client;

	// zookeeper根路径节点
	private static final String ROOT_PATH = "MyRPC";

	private final LoadBalancer loadBalancer;

	private List<String> repos = new ArrayList<>();

	public ZkServiceRegistry() {
		this(null);
	}

	public ZkServiceRegistry(LoadBalancer loadBalancer) {
		if(loadBalancer == null) {
			this.loadBalancer = new RandomLoadBalance();
		} else {
			this.loadBalancer = loadBalancer;
		}
	}

	//连接zookeeper
	{
		// 重试策略
		RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
		this.client = CuratorFrameworkFactory
				.builder()
				// 服务器列表
				.connectString("112.74.188.132:2181")
				// 会话超时时间
				.sessionTimeoutMs(60 * 1000)
				// 连接创建超时时间
				.connectionTimeoutMs(15 * 1000)
				.retryPolicy(policy)
				.namespace(ROOT_PATH)
				.build();
		this.client.start();
		logger.info("zookeeper连接成功");

	}

	@Override
	public void register(String serviceName, InetSocketAddress inetSocketAddress) {
		// 注册相应服务
		try {

			//例：servicePath = /com.whc.rpc.api.UserService
			String servicePath = "/" +serviceName;
			//如果节点不存在，则创建
			if(client.checkExists().forPath(servicePath) == null) {
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath,"0".getBytes());
			}

			//例：inetSocketAddress = /com.whc.rpc.api.UserService/127.0.0.1:9000
			String path = servicePath + inetSocketAddress;

			logger.info(path);
			//创建临时节点：/MyRPC/com.whc.rpc.api.UserService/127.0.0.1:9000
			String rsNode = client.create().withMode(CreateMode.EPHEMERAL).forPath(path,"0".getBytes());
			logger.error("服务注册成功："+rsNode);

		} catch (Exception e) {
			logger.error("服务注册失败,此服务已存在");
			throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
		}
	}

	// 根据服务名返回地址
	@Override
	public InetSocketAddress serviceDiscovery(String serviceName) {
		String path = "/" + serviceName;

		try {
			repos = client.getChildren().forPath(path);

			// 动态发现服务节点的变化(监听), 如果提供服务的服务端上下线,则重新更新服务器列表
			registerWatcher(path);

			// 负载均衡机制
			String string = loadBalancer.balance(repos);
			logger.info("根据负载均衡策略后, 返回的服务器ip地址为:" + string);
			return parseAddress(string);
		} catch (Exception e) {
			logger.error("获取服务时有错误发生:", e);
		}
		return null;
	}

	private void registerWatcher(String path) {
		// 1. 创建监听对象
		PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);

		// 2. 绑定监听器
		pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
				logger.info("子节点发生了变化..");
				repos = curatorFramework.getChildren().forPath(path);
			}
		});

		// 3. 开启
		try {
			pathChildrenCache.start();
		} catch (Exception e) {
			logger.error("注册PathChild Watcher异常:", e);
		}
	}


	// 地址 -> XXX.XXX.XXX.XXX:port 字符串
	private String getServiceAddress(InetSocketAddress serverAddress) {
		return serverAddress.getHostName() +
				":" +
				serverAddress.getPort();
	}

	// 字符串解析为地址
	private InetSocketAddress parseAddress(String address) {
		String[] result = address.split(":");
		return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
	}*/
}
