package com.whc.rpc.loadbalance.loadbalancer;

import com.whc.rpc.loadbalance.AbstractLoadBalance;

import java.util.List;

/**
 * 轮询负载均衡
 * @ClassName: RoundLoadBalance
 * @Author: whc
 * @Date: 2021/06/12/22:12
 */
public class RoundLoadBalance extends AbstractLoadBalance {

	private int index = 0;

	@Override
	protected String doSelect(List<String> serviceAddresses) {
		if(index >= serviceAddresses.size()) {
			index %= serviceAddresses.size();
		}
		return serviceAddresses.get(index++);
	}
}
