package com.whc.rpc.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * 单例工厂
 * @ClassName: SingletonFactory
 * @Author: whc
 * @Date: 2021/06/10/1:12
 */
public class SingletonFactory {

	private static Map<Class, Object> objectMap = new HashMap<>();

	private SingletonFactory() {}

	public static <T> T getInstance(Class<T> clazz) {
		Object instance = objectMap.get(clazz);
		synchronized (clazz) {
			if(instance == null) {
				try {
					instance = clazz.newInstance();
					objectMap.put(clazz, instance);
				} catch (IllegalAccessException | InstantiationException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
		return clazz.cast(instance);
	}
}
