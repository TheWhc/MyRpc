package com.whc.rpc.exception;

import com.whc.rpc.enumeration.RpcError;

/**
 * RPC调用异常
 * @ClassName: RpcException
 * @Author: whc
 * @Date: 2021/05/24/18:57
 */
public class RpcException extends RuntimeException {

	public RpcException(RpcError error, String detail) {
		super(error.getMessage() + ": " + detail);
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(RpcError error) {
		super(error.getMessage());
	}
}
