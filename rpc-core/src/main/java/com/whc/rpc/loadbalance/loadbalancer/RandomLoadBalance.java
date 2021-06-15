package com.whc.rpc.loadbalance.loadbalancer;

import com.whc.rpc.loadbalance.AbstractLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡
 * @ClassName: RandomLoadBalance
 * @Author: whc
 * @Date: 2021/06/12/22:11
 */
public class RandomLoadBalance extends AbstractLoadBalance {

	@Override
	protected String doSelect(List<String> serviceAddresses) {
		return serviceAddresses.get(new Random().nextInt(serviceAddresses.size()));
	}

}
