package com.whc.rpc.serializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whc.rpc.entity.RpcRequest;
import com.whc.rpc.enumeration.SerializerCode;
import com.whc.rpc.exception.SerializeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 使用JSON格式的序列化器
 * @ClassName: JsonSerializer
 * @Author: whc
 * @Date: 2021/05/29/20:51
 */
public class JsonSerializer implements CommonSerializer{

	private static final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public byte[] serialize(Object obj) {
		try {
			// 对象转byte数组
			return objectMapper.writeValueAsBytes(obj);
		} catch (JsonProcessingException e) {
			logger.error("序列化时有错误发生:", e);
			throw new SerializeException("序列化时有错误发生");
		}
	}

	@Override
	public Object deserialize(byte[] bytes, Class<?> clazz) {
		try {
			// byte数组转对象
			Object obj = objectMapper.readValue(bytes, clazz);
			if(obj instanceof RpcRequest) {
				obj = handleRequest(obj);
			}
			return obj;
		} catch (IOException e) {
			logger.error("序列化时有错误发生:", e);
			throw new SerializeException("序列化时有错误发生");
		}
	}

	/**
	 *  由于这里使用JSON序列化和反序列化时Object数组,无法保证反序列化时后仍然为原实例类型
	 *  需要重新判断处理
	 */
	private Object handleRequest(Object obj) throws IOException {
		RpcRequest rpcRequest = (RpcRequest)obj;
		for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
			Class<?> clazz = rpcRequest.getParamTypes()[i];
			if(!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
				byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
				rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
			}
		}
		return rpcRequest;
	}

	@Override
	public int getCode() {
		return SerializerCode.valueOf("JSON").getCode();
	}
}
