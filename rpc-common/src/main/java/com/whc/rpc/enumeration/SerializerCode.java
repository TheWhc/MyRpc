package com.whc.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: SerializerCode
 * @Author: whc
 * @Date: 2021/05/29/20:58
 */
@AllArgsConstructor
@Getter
public enum  SerializerCode {

	KRYO(0),
	JSON(1),
	HESSIAN(2),
	PROTOBUF(3);

	private final int code;
}
