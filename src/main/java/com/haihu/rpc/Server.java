package com.haihu.rpc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Server {
	public static void main(String[] args) {
//		RpcNettyServer rpcNettyServer = new RpcNettyServer(8081);
//		rpcNettyServer.startServer();
		
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath*:/rpc/spring-server-bean.xml");
    	context.start();
	}
}
