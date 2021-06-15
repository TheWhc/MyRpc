package com.whc.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RPC调用过程中的错误
 * @ClassName: RpcError
 * @Author: whc
 * @Date: 2021/05/24/18:55
 */
@AllArgsConstructor
@Getter
public enum  RpcError {

	SERVICE_INVOCATION_FAILURE("服务调用出现失败"),
	SERVICE_NOT_FOUND("找不到对应的服务"),
	SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务未实现接口"),
	UNKNOWN_PROTOCOL("不识别的协议包"),
	UNKNOWN_PACKAGE_TYPE("不识别的数据包类型"),
	UNKNOWN_SERIALIZER("不识别的(反)序列化器"),
	SERIALIZER_NOT_FOUND("找不到序列化器"),
	RESPONSE_NOT_MATCH("响应和请求号不匹配"),
	REGISTER_SERVICE_FAILED("注册服务失败"),
	SERVICE_SCAN_PACKAGE_NOT_FOUND("扫描不到包"),
	UNKNOWN_ERROR("未知错误");

	private final String message;
}
