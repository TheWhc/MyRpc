package com.whc.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: User
 * @Author: whc
 * @Date: 2021/05/25/23:08
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
	// 客户端和服务端共有的
	private Integer id;
	private String userName;
	private Boolean sex;
}
