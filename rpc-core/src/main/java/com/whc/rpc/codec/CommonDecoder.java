package com.whc.rpc.codec;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.entity.RpcResponse;
import com.whc.rpc.enumeration.PackageType;
import com.whc.rpc.enumeration.RpcError;
import com.whc.rpc.exception.RpcException;
import com.whc.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 通用的解码拦截器
 * 完成 ByteBuf 到 POJO 对象的解码
 * @ClassName: CommonDecoder
 * @Author: whc
 * @Date: 2021/05/29/21:24
 */
public class CommonDecoder extends ReplayingDecoder {

	private static final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

	private static final int MAGIC_NUMBER = 0xCAFEBABE;


	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int magic = in.readInt();
		if(magic != MAGIC_NUMBER) {
			logger.error("不识别的协议包:{}", magic);
			throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
		}
		int packageCode = in.readInt();
		Class<?> packageClass;
		if(packageCode == PackageType.REQUEST_PACK.getCode()) {
			packageClass = RpcRequest.class;
		} else if(packageCode == PackageType.RESPONSE_PACK.getCode()) {
			packageClass = RpcResponse.class;
		} else {
			logger.error("不识别的数据包:{}", packageCode);
			throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
		}
		int serializerCode = in.readInt();
		// 获取序列化器类型
		CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
		if(serializer == null) {
			logger.error("不识别的反序列化器:{}", serializerCode);
			throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
		}
		// 数据长度
		int length = in.readInt();
		byte[] bytes = new byte[length];
		// 填充数据
		in.readBytes(bytes);
		// 反序列化
		Object obj = serializer.deserialize(bytes, packageClass);
		out.add(obj);
	}
}
