package com.whc.rpc;

import com.whc.rpc.serializer.CommonSerializer;

/**
 * @ClassName: RpcServer
 * @Author: whc
 * @Date: 2021/05/29/20:13
 */
public interface RpcServer {

	void start(int port);

	void setSerializer(CommonSerializer serializer);
}
