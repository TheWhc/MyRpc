package com.whc.rpc.hook;

import com.whc.rpc.registry.zk.util.CuratorUtils;
import com.whc.rpc.transport.netty.server.NettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 当服务器关闭时，执行一些操作，例如取消注册所有服务
 * @ClassName: CustomShutdownHook
 * @Author: whc
 * @Date: 2021/06/14/2:19
 */
public class CustomShutdownHook {

	private static final Logger logger = LoggerFactory.getLogger(CustomShutdownHook.class);

	private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

	public static CustomShutdownHook getCustomShutdownHook() {
		return CUSTOM_SHUTDOWN_HOOK;
	}

	public void clearAll() {
		logger.info("ShutdownHook 优雅停机");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				// 假设9000端口对应的服务机器宕机了,则注销服务元数据
				InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyServer.PORT);
				CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}));
	}
}
