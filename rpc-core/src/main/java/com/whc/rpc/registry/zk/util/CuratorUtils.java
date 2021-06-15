package com.whc.rpc.registry.zk.util;

import com.whc.rpc.enumeration.RpcConfigEnum;
import com.whc.rpc.util.PropertiesFileUtil;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 创建Zk客户端类、创建节点、获取节点、监听节点、清除注册表工具类
 * @ClassName: CuratorUtils
 * @Author: whc
 * @Date: 2021/06/13/23:54
 */
public class CuratorUtils {

	private static final Logger logger = LoggerFactory.getLogger(CuratorUtils.class);

	private static final int BASE_SLEEP_TIME = 1000;
	private static final int MAX_RETRIES = 3;
	public static final String ZK_REGISTER_ROOT_PATH = "/MyRPC";
	// 客户端本地服务缓存
	private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
	private static final Set<String> PERSISTENT_REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
	private static final Set<String> EPHEMERAL_REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
	private static CuratorFramework zkClient;
	private static final String DEFAULT_ZOOKEEPER_ADDRESS = "112.74.188.132:2181";

	private CuratorUtils() {}

	// 创建服务名永久节点PERSISTENT
	public static void createPersistentNode(CuratorFramework zkClient, String path) {
		try {
			// 永久节点已存在
			if (PERSISTENT_REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
				logger.info("永久节点已经存在,永久节点是:[{}]", path);
			} else {
				// 永久节点不存在,则创建永久节点
				//eg: /MyRPC/com.whc.rpc.api.UserService
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
				logger.info("永久节点成功被创建,永久节点是:[{}]", path);
			}
			PERSISTENT_REGISTERED_PATH_SET.add(path);
		} catch (Exception e) {
			logger.error("创建永久节点失败[{}]", path);
		}
	}

	// 创建服务地址为临时节点EPHEMERAL
	// 临时节点，当客户端与 Zookeeper 之间的连接或者 session 断掉时会被zk自动删除。开源 Dubbo 框架，使用的就是临时节点
	// 优点: 当服务节点下线或者服务节点不可用，Zookeeper 会自动将节点地址信息从注册中心删除
	public static void createEphemeralNode(CuratorFramework zkClient, String path) {
		try {
			// 临时节点已存在
			if (EPHEMERAL_REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
				logger.info("临时节点已经存在,临时节点是:[{}]", path);
			} else {
				// 临时节点不存在,则创建临时节点
				//eg: /MyRPC/com.whc.rpc.api.UserService/127.0.0.1:9000
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
				logger.info("临时节点成功被创建,临时节点是:[{}]", path);
			}
			EPHEMERAL_REGISTERED_PATH_SET.add(path);
		} catch (Exception e) {
			logger.error("创建临时节点失败[{}]", path);
		}
	}

	// 获取一个节点下的孩子节点
	public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
		if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
			return SERVICE_ADDRESS_MAP.get(rpcServiceName);
		}
		List<String> result = null;
		String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
		try {
			result = zkClient.getChildren().forPath(servicePath);
			SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
			// 动态发现服务节点的变化(监听),如果提供服务的服务端上下线,则重新更新服务器列表
			registerWatcher(rpcServiceName, zkClient);
		} catch (Exception e) {
			logger.error("获取节点下的孩子节点 [{}] 失败", servicePath);
		}
		return result;
	}

	// 对节点进行注册监听, 用的是PathChildrenCache
	private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
		String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
		// 1. 创建监听对象
		PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);

		// 2. 绑定监听器
		pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
				// 重新获取节点的孩子节点, 即重新获取服务列表信息
				List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
				// 更新客户端本地服务缓存
				SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
				logger.info("服务地址列表:{}", SERVICE_ADDRESS_MAP.get(rpcServiceName));
			}
		});

		// 3. 开启
		pathChildrenCache.start();
	}

	public static CuratorFramework getZkClient() {
		// 检查是否设置过zk地址
		Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
		String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
		// 如果zkClient已经设置过,立即返回
		if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
			return zkClient;
		}
		// 重试策略
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
		zkClient = CuratorFrameworkFactory.builder()
				.connectString(zookeeperAddress)
				.retryPolicy(retryPolicy)
				.build();
		zkClient.start();
		try {
			// 等待30秒直到连接上zk
			if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
				throw new RuntimeException("连接ZK超时!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return zkClient;
	}
}
