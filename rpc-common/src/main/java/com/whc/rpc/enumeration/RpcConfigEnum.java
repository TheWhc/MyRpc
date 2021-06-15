package com.whc.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: RpcConfigEnum
 * @Author: whc
 * @Date: 2021/06/14/0:29
 */
@AllArgsConstructor
@Getter
public enum  RpcConfigEnum {

	RPC_CONFIG_PATH("rpc.properties"),
	ZK_ADDRESS("rpc.zookeeper.address");

	private final String propertyValue;

}
