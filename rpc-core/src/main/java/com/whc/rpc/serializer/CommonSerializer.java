package com.whc.rpc.serializer;

/**
 * @ClassName: CommonSerializer
 * @Author: whc
 * @Date: 2021/05/29/20:50
 */
public interface CommonSerializer {

	static CommonSerializer getByCode(int code) {
		switch (code) {
			case 0:
				return new KryoSerializer();
			case 1:
				return new JsonSerializer();
			case 2:
				return new HessianSerializer();
			default:
				return null;
		}
	}

	byte[] serialize(Object obj);

	Object deserialize(byte[] bytes, Class<?> clazz);

	int getCode();
}
