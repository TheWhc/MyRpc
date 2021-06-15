package com.whc.rpc.transport;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.serializer.CommonSerializer;

/**
 * 客户端通用接口
 * @ClassName: RpcClient
 * @Author: whc
 * @Date: 2021/05/29/20:12
 */
public interface RpcClient {

	int DEFAULT_SERIALIZER = CommonSerializer.KRYO_SERIALIZER;

	Object sendRequest(RpcRequest rpcRequest);

}
