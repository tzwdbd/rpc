package com.haihu.rpc.common;

import io.netty.channel.Channel;

public interface ChannelStatusListener {
	
	public void channelDisconnect(Channel channel);
	
	public void channelConnect(Channel channel);

}
