package com.whc.rpc.transport.socket.server;

import com.whc.rpc.transport.RpcServer;
import com.whc.rpc.handler.RequestHandler;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.factory.ThreadPoolFactory;
import com.whc.rpc.provider.ServiceProvider;
import com.whc.rpc.provider.ServiceProviderImpl;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.registry.ZkServiceRegistry;
import com.whc.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @ClassName: SocketServer
 * @Author: whc
 * @Date: 2021/05/29/20:20
 */
public class SocketServer implements RpcServer {

	private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	private final ExecutorService threadPool;
	private final String host;
	private final int port;
	private CommonSerializer serializer;
	private RequestHandler requestHandler = new RequestHandler();

	private final ServiceRegistry serviceRegistry;
	private final ServiceProvider serviceProvider;

	public SocketServer(String host, int port) {
		this(host, port, DEFAULT_SERIALIZER);
	}

	public SocketServer(String host, int port, Integer serializer) {
		this.host = host;
		this.port = port;
		threadPool = ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
		this.serviceRegistry = new ZkServiceRegistry();
		this.serviceProvider = new ServiceProviderImpl();
		this.serializer = CommonSerializer.getByCode(serializer);
	}

	@Override
	public <T> void publishService(T service, Class<T> serviceClass) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}
		serviceProvider.addServiceProvider(service, serviceClass);
		serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
		start();
	}

	@Override
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			logger.info("服务器启动……");
			Socket socket;
			// BIO的方式监听Socket
			while((socket = serverSocket.accept()) != null) {
				logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
				// 线程池创建线程处理消费者的请求
				threadPool.execute(new SocketRequestHandlerThread(socket, requestHandler, serializer));
			}
			threadPool.shutdown();
		} catch (IOException e) {
			logger.error("服务器启动时有错误发生:", e);
		}
	}
}
