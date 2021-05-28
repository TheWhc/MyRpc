package com.whc.test;

import com.whc.rpc.api.Blog;
import com.whc.rpc.api.BlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: BlogServiceImpl
 * @Author: whc
 * @Date: 2021/05/28/20:02
 */
public class BlogServiceImpl implements BlogService {

	private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

	@Override
	public Blog getBlogById(Integer id) {
		Blog blog = Blog.builder().id(id).title("我的博客").userId(22).build();
		logger.info("客户端查询了{}博客", id);
		return blog;
	}
}
