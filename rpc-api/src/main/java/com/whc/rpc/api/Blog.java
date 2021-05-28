package com.whc.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: Blog
 * @Author: whc
 * @Date: 2021/05/28/20:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog implements Serializable {
	private Integer id;
	private Integer userId;
	private String title;
}
