package com.whc.rpc.socket.server;

import com.whc.rpc.RpcServer;
import com.whc.rpc.RequestHandler;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.serializer.CommonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 50;
	private static final int KEEP_ALIVE_TIME = 60;
	private static final int BLOCKING_QUEUE_CAPACITY = 100;
	private final ExecutorService threadPool;
	private RequestHandler requestHandler = new RequestHandler();
	private CommonSerializer serializer;
	private final ServiceRegistry serviceRegistry;

	public SocketServer(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workingQueue, threadFactory);
	}

	@Override
	public void start(int port) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			logger.info("服务器启动……");
			Socket socket;
			// BIO的方式监听Socket
			while((socket = serverSocket.accept()) != null) {
				logger.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
				// 线程池创建线程处理消费者的请求
				threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry, serializer));
			}
			threadPool.shutdown();
		} catch (IOException e) {
			logger.error("服务器启动时有错误发生:", e);
		}
	}

	@Override
	public void setSerializer(CommonSerializer serializer) {
		this.serializer = serializer;
	}
}
