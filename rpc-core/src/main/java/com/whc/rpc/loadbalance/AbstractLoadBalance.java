package com.whc.rpc.loadbalance;

import java.util.List;

/**
 * @ClassName: AbstractLoadBalance
 * @Author: whc
 * @Date: 2021/06/14/1:05
 */
public abstract class AbstractLoadBalance implements LoadBalancer {

	@Override
	public String balance(List<String> serviceAddresses) {
		if (serviceAddresses == null || serviceAddresses.size() == 0) {
			return null;
		}
		if (serviceAddresses.size() == 1) {
			return serviceAddresses.get(0);
		}
		return doSelect(serviceAddresses);
	}

	protected abstract String doSelect(List<String> serviceAddresses);
}
