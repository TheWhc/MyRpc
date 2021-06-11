package com.whc.rpc.transport.socket.client;

import com.whc.rpc.transport.RpcClient;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.ResponseCode;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.registry.ServiceRegistry;
import com.whc.rpc.registry.ZkServiceRegistry;
import com.whc.rpc.serializer.CommonSerializer;
import com.whc.rpc.transport.socket.util.ObjectReader;
import com.whc.rpc.transport.socket.util.ObjectWriter;
import com.whc.rpc.util.RpcMessageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Socket方式远程方法调用的消费者(客户端)
 * @ClassName: SocketClient
 * @Author: whc
 * @Date: 2021/05/29/20:15
 */
public class SocketClient implements RpcClient {

	private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

	private final ServiceRegistry serviceDiscovery;

	private final CommonSerializer serializer;

	public SocketClient(Integer serializer) {
		this.serviceDiscovery = new ZkServiceRegistry();
		this.serializer = CommonSerializer.getByCode(serializer);
	}

	@Override
	public Object sendRequest(RpcRequest rpcRequest) {
		if(serializer == null) {
			logger.error("未设置序列化器");
			throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
		}

		InetSocketAddress inetSocketAddress = serviceDiscovery.serviceDiscovery(rpcRequest.getInterfaceName());

		try (Socket socket = new Socket()) {
			socket.connect(inetSocketAddress);
			OutputStream outputStream = socket.getOutputStream();
			InputStream inputStream = socket.getInputStream();
			ObjectWriter.writeObject(outputStream, rpcRequest, serializer);
			Object obj = ObjectReader.readObject(inputStream);
			RpcResponse rpcResponse = (RpcResponse) obj;
			if (rpcResponse == null) {
				logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
				throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
			}
			if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
				logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
				throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
			}
			RpcMessageChecker.check(rpcRequest, rpcResponse);
			return rpcResponse;
		} catch (IOException e) {
			logger.error("调用时有错误发生：", e);
			throw new RpcException("服务调用失败: ", e);
		}

	}

}
