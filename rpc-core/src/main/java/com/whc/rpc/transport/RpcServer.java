package com.whc.rpc.transport;

import com.whc.rpc.serializer.CommonSerializer;

/**
 * 服务器类通用接口
 * @ClassName: RpcServer
 * @Author: whc
 * @Date: 2021/05/29/20:13
 */
public interface RpcServer {

	int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

	void start();

	<T> void publishService(T service, Class<T> serviceClass);

}
