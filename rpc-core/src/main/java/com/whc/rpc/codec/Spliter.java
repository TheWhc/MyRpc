package com.whc.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Netty 提供了 LengthFieldBasedFrameDecoder，自动屏蔽 TCP 底层的拆包和粘包问题，只需要传入正确的参数，即可轻松解决“读半包“问题。
 *
 * Spliter作用:
 * 1. 基于固定长度域的拆包器,根据我们的自定义协议,把数据拼装成一个个符合我们自定义数据包大小的ByteBuf,接着根据我们的自定义协议解码器去解码
 * 2. 拒绝非本协议连接
 *
 * @ClassName: Spliter
 * @Author: whc
 * @Date: 2021/06/04/22:16
 */
public class Spliter extends LengthFieldBasedFrameDecoder {

	private static final int MAGIC_NUMBER = 0xCAFEBABE;

	// 消息格式为: [魔数][数据包类型][序列化器类型][数据长度][数据]
	//			  4字节   4字节      4字节       4字节
	private static final int LENGTH_FIELD_OFFSET = 12;
	// 数据长度
	private static final int LENGTH_FIELD_LENGTH = 4;

	public Spliter() {
		super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		if(in.getInt(in.readerIndex()) != MAGIC_NUMBER) {
			ctx.channel().closeFuture();
			return null;
		}
		return super.decode(ctx, in);
	}
}

