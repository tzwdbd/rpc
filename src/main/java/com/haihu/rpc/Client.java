package com.haihu.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.haihu.rpc.common.SpringObjectFactory;

public class Client {
	
	private SpringObjectFactory springObjectFactory;

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/rpc/spring-client-bean.xml");
    	context.start();
		
//		RpcNettyClient HHNettyRpcClient = new RpcNettyClient("localhost",8081);
//		HHNettyRpcClient.start();
	}
}
