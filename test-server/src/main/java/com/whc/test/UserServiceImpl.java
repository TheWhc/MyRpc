package com.whc.test;

import com.whc.rpc.api.User;
import com.whc.rpc.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

/**
 * @ClassName: UserServiceImpl
 * @Author: whc
 * @Date: 2021/05/25/23:08
 */
public class UserServiceImpl implements UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	@Override
	public User getUserByUserId(Integer id) {
		logger.info("接收到用户id：{}", id);
		// 模拟从数据库中获取用户的行为
		Random random = new Random();
		User user = User.builder()
				.userName("张三")
				.id(id)
				.sex(random.nextBoolean())
				.build();
		// 返回查询对象
		return user;
	}

	@Override
	public Integer insertUserId(User user) {
		logger.info("插入用户:{}", user);
		return 1;
	}
}
