package com.whc.rpc.loadbalance;


import java.util.List;

/**
 * 负载均衡接口
 * 给服务器地址列表,根据不同的负载均衡策略选择一个
 * @ClassName: LoadBalancer
 * @Author: whc
 * @Date: 2021/06/12/22:08
 */
public interface LoadBalancer {
	String balance(List<String> serviceAddresses);
}
