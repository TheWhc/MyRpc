package com.whc.rpc.loadbalancer;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * @ClassName: RandomLoadBalance
 * @Author: whc
 * @Date: 2021/06/12/22:11
 */
public class RandomLoadBalance implements LoadBalancer {
	@Override
	public String balance(List<String> addressList) {
		return addressList.get(new Random().nextInt(addressList.size()));
	}
}
