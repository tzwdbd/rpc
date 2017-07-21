package com.haihu.rpc.client;

import java.lang.reflect.Proxy;

import javax.annotation.Resource;

import com.haihu.rpc.common.RpcCallback;
import com.haihu.rpc.common.RpcInvoker;


public class RpcClientProxy {
	@Resource
	private RpcNettyClient rpcNettyClient;
	
	
	@SuppressWarnings("unchecked")
	public <T> T wrapProxy(Class<T> interfaceClass,RpcCallback callback) {
		if (interfaceClass == null) {
			throw new IllegalArgumentException("serviceInterface can not be null.");
		} else if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException("serviceInterface is required to be interface.");
		}
		RpcInvoker invoker = new RpcInvoker(callback);
		rpcNettyClient.addRpcInvoker(invoker);
		// 创建代理
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] { interfaceClass }, invoker);
	}

}
