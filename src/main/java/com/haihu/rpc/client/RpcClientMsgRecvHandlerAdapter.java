package com.haihu.rpc.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.common.CallableImpl;
import com.haihu.rpc.common.ChannelStatusListener;
import com.haihu.rpc.common.FutureTaskImpl;
import com.haihu.rpc.common.RpcBaseHandlerAdapter;
import com.haihu.rpc.common.RpcRequest;

@Sharable
public class RpcClientMsgRecvHandlerAdapter extends RpcBaseHandlerAdapter {
	
	private static Log log = LogFactory.getLog(RpcClientMsgRecvHandlerAdapter.class);
    protected static Semaphore lock = new Semaphore(1);
    private Channel channel;
    
    @Override
    public void closeMore(){
    	if(channel != null){
    		try{
    			channel.close();
    		}catch(Throwable e){
    			log.error("MessageRecvHandlerAdapter.close",e);
    		}
    	}
    }
    
    public RpcClientMsgRecvHandlerAdapter(ChannelStatusListener channelStatusListener){
    	super(channelStatusListener);
    }
    
    public boolean isBusy(){
    	if(lock.tryAcquire()){
    		lock.release();
    		return false;
    	}else{
    		return true;
    	}
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
    }
    

    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	super.channelRead(ctx, msg);
    	if(msg instanceof RpcRequest){//rpc请求消息
    		RpcRequest rpcRequest = (RpcRequest) msg;
    		if(lock.tryAcquire()){
    			sendAck(ctx.channel(),(byte)1, (byte)1, rpcRequest.getRequestId());
    			FutureTaskImpl<Object> future = new FutureTaskImpl<Object>(new CallableImpl(rpcRequest,lock,msgRespMap));
    			fixedThreadPool.execute(future);
    			synchronized (futureList) {
    				futureList.add(future);
				}
    		}else{
    			sendAck(ctx.channel(),(byte)1, (byte)2, rpcRequest.getRequestId());//普通应答包
    		}
    	}
    }
    
    @Override
    public Channel getChannel(String ipAddress){
    	return channel;
    }
}
