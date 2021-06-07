package com.whc.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: PackageType
 * @Author: whc
 * @Date: 2021/05/29/21:23
 */
@AllArgsConstructor
@Getter
public enum  PackageType {
	REQUEST_PACK(0),
	RESPONSE_PACK(1);

	private final int code;
}
