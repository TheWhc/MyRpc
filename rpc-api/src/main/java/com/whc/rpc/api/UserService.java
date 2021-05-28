package com.whc.rpc.api;

/**
 * @ClassName: UserService
 * @Author: whc
 * @Date: 2021/05/25/23:08
 */
public interface UserService {
	// 客户端通过这个接口调用服务端的实现类
	User getUserByUserId(Integer id);
	// 给这个服务增加一个功能
	Integer insertUserId(User user);
}
