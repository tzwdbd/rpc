package com.haihu.rpc.common;


public interface HeartBeatListener {
	
	public void channelDisconnect(String ip,String heartBeartRequestId);
	
	public void channelConnect(String version,String ip,String heartBeartRequestId);

}
