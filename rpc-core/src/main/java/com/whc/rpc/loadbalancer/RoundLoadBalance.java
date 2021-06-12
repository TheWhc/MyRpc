package com.whc.rpc.loadbalancer;

import java.util.List;

/**
 * 轮询负载均衡
 * @ClassName: RoundLoadBalance
 * @Author: whc
 * @Date: 2021/06/12/22:12
 */
public class RoundLoadBalance implements LoadBalancer{

	private int index = 0;

	@Override
	public String balance(List<String> addressList) {
		if(index >= addressList.size()) {
			index %= addressList.size();
		}
		return addressList.get(index++);
	}
}
