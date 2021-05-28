package com.whc.rpc.server;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.registry.ServiceRegistry;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程,从服务端代码分离出来,简化服务端代码,单一职责原则
 * @ClassName: RequestHandlerThread
 * @Author: whc
 * @Date: 2021/05/24/20:27
 *
 * 这里负责解析得到的request请求,执行服务方法,返回给客户端
 * 1. 从request得到interfaceName
 * 2. 根据interfaceName在serviceRegistry中获取服务端的实现类
 * 3. 将request请求和获取到的实现类交给requestHandler处理器处理,进一步抽离代码
 * 4. 获取处理器返回的结果,封装成response对象,写入socket
 */
@AllArgsConstructor
public class RequestHandlerThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

	private Socket socket;
	private RequestHandler requestHandler;
	private ServiceRegistry serviceRegistry;

	@Override
	public void run() {
		try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
			RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
			// 接口名称
			String interfaceName = rpcRequest.getInterfaceName();
			// 接口实现类
			Object service = serviceRegistry.getService(interfaceName);
			Object result = requestHandler.handle(rpcRequest, service);
			objectOutputStream.writeObject(RpcResponse.success(result));
			objectOutputStream.flush();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("调用或发送时有错误发生：", e);
		}
	}

}
