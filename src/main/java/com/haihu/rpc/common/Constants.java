package com.haihu.rpc.common;

public class Constants {
	
	public static final int HEART_WRITE_TIMEOUT = 12;//客户端心跳写超时
	
	public static final int HEART_RETRY_TIME = 3;//客户端心跳写超时次数
	
	public static final int HEART_READ_TIMEOUT = (HEART_RETRY_TIME+1) * HEART_WRITE_TIMEOUT; //服务器心跳读超时
	
	public static final int RE_CONN_WAIT_SECONDS = 10;
	
	public static final int REQUEST_TIMEOUT = 15000;//请求超时 毫秒

}
