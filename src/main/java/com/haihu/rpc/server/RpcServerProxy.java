package com.haihu.rpc.server;

import java.lang.reflect.Proxy;

import javax.annotation.Resource;

import com.haihu.rpc.common.RpcCallback;
import com.haihu.rpc.common.RpcInvoker;

public class RpcServerProxy {
	
	@Resource
	private RpcNettyServer rpcNettyServer;

	
	@SuppressWarnings("unchecked")
	public <T> T wrapProxy(Class<T> interfaceClass,String ipAddress,RpcCallback callback) {
		if (interfaceClass == null) {
			throw new IllegalArgumentException("serviceInterface can not be null.");
		} else if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException("serviceInterface is required to be interface.");
		}
		RpcInvoker invoker = new RpcInvoker(callback);
		rpcNettyServer.addRpcInvoker(invoker,ipAddress);
		// 创建代理
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] { interfaceClass }, invoker);
	}

}
