package com.whc.rpc.transport.socket.server;

import com.whc.rpc.handler.RequestHandler;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.serializer.CommonSerializer;
import com.whc.rpc.transport.socket.util.ObjectReader;
import com.whc.rpc.transport.socket.util.ObjectWriter;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程,从服务端代码分离出来,简化服务端代码,单一职责原则
 * @ClassName: SocketRequestHandlerThread
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
public class SocketRequestHandlerThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SocketRequestHandlerThread.class);

	private Socket socket;
	private RequestHandler requestHandler;
	private CommonSerializer serializer;

	@Override
	public void run() {
		try(InputStream inputStream = socket.getInputStream()) {
			OutputStream outputStream = socket.getOutputStream();
			RpcRequest rpcRequest = (RpcRequest) ObjectReader.readObject(inputStream);
			Object result = requestHandler.handle(rpcRequest);
			RpcResponse<Object> response = RpcResponse.success(result, rpcRequest.getRequestId());
			ObjectWriter.writeObject(outputStream, response, serializer);
		} catch (IOException e) {
			logger.error("调用或发送时有错误发生：", e);
		}
	}

}
