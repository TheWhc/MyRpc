package com.whc.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @ClassName: PropertiesFileUtil
 * @Author: whc
 * @Date: 2021/06/14/0:32
 */
public class PropertiesFileUtil {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesFileUtil.class);

	private PropertiesFileUtil() {
	}

	public static Properties readPropertiesFile(String fileName) {
		URL url = Thread.currentThread().getContextClassLoader().getResource("");
		String rpcConfigPath = "";
		if (url != null) {
			rpcConfigPath = url.getPath() + fileName;
		}
		Properties properties = null;
		try (InputStreamReader inputStreamReader = new InputStreamReader(
				new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
			properties = new Properties();
			properties.load(inputStreamReader);
		} catch (IOException e) {
			logger.error("读取配置文件发生错误 [{}]", fileName);
		}
		return properties;
	}
}
