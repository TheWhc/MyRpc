package com.whc.rpc.client;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.ResponseCode;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 远程方法调用的消费者（客户端）
 * @ClassName: RpcClient
 * @Author: whc
 * @Date: 2021/05/24/18:59
 */
public class RpcClient {

	private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

	public Object sendRequest(RpcRequest rpcRequest, String host, int port) {
		try (Socket socket = new Socket(host, port)) {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
			objectOutputStream.writeObject(rpcRequest);
			objectOutputStream.flush();
			RpcResponse rpcResponse = (RpcResponse) objectInputStream.readObject();
			if(rpcResponse == null) {
				logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
				throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
			}
			if(rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
				logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
				throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
			}
			return rpcResponse.getData();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("调用时有错误发生：", e);
			throw new RpcException("服务调用失败: ", e);
		}
	}
}
