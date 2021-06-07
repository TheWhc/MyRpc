package com.whc.rpc.codec;

import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.enumeration.PackageType;
import com.whc.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 通信协议的设计
 * 通用的编码拦截器
 * 负责将 POJO 对象编码成 ByteBuf
 * @ClassName: CommonEncoder
 * @Author: whc
 * @Date: 2021/05/29/20:48
 */
public class CommonEncoder extends MessageToByteEncoder {

	private static final int MAGIC_NUMBER = 0xCAFEBABE;

	private final CommonSerializer serializer;

	public CommonEncoder(CommonSerializer serializer) {
		this.serializer = serializer;
	}

	// 自定义传输协议,防止粘包
	// 消息格式为: [魔数][数据包类型][序列化器类型][数据长度][数据]
	//			  4字节   4字节      4字节       4字节
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		// 魔数
		out.writeInt(MAGIC_NUMBER);
		// 数据包类型
		if (msg instanceof RpcRequest) {
			out.writeInt(PackageType.REQUEST_PACK.getCode());
		} else {
			out.writeInt(PackageType.RESPONSE_PACK.getCode());
		}
		// 序列化器类型
		out.writeInt(serializer.getCode());
		byte[] bytes = serializer.serialize(msg);
		// 数据长度
		out.writeInt(bytes.length);
		// 数据
		out.writeBytes(bytes);
	}
}
