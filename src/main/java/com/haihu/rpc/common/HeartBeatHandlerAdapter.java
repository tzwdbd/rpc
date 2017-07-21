package com.haihu.rpc.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.service.HeartBeatService;

@Sharable
public class HeartBeatHandlerAdapter extends ChannelInboundHandlerAdapter {
	private  Log log = LogFactory.getLog(getClass());
	public int unRecPongTimes = 0;
	private volatile String version;
	private HeartBeatListener heartBeatListener;
	private String heartBeatRequestId;
	private Map<Channel,String> channelHeartBeatMap = new ConcurrentHashMap<Channel, String>();
	
	public HeartBeatHandlerAdapter(){
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public HeartBeatListener getHeartBeatListener() {
		return heartBeatListener;
	}

	public void setHeartBeatListener(HeartBeatListener heartBeatListener) {
		this.heartBeatListener = heartBeatListener;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
        	
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {//for client
                /*写超时*/   
                if(unRecPongTimes < Constants.HEART_RETRY_TIME){
                	log.error("发送心跳包");
                	RpcRequest rpcRequest = getHeartBeatRequest(HeartBeatService.class);
                	if(rpcRequest != null){
                		try{
                			ctx.channel().writeAndFlush(rpcRequest);
                		}catch(Exception e){
                			ctx.channel().close();
                			log.error(e);
                		}
                	}
                	unRecPongTimes++;
                }else{
                	log.error("心跳包三次超时,关闭channel");
                	ctx.channel().close();
                }
            }else if(event.state() == IdleState.READER_IDLE){//for server
            	//没有收到客户端的心跳包,断开请求
            	log.error("服务端没有收到心跳包,关闭channel");
            	ctx.channel().close();
            }
        }
    }
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		boolean isHeartBeatresp = false;
    	if(msg instanceof RpcRequest){//rpc心跳请求消息
    		final RpcRequest rpcRequest = (RpcRequest) msg;
    		if(HeartBeatService.class.getName().equals(rpcRequest.getClassName())){//心跳包请求
//    			log.error("发送心跳回执 = "+Thread.currentThread().getId());
    			isHeartBeatresp = true;
    			sendHeartAck(ctx.channel(),rpcRequest.getRequestId(),rpcRequest.getClassName());
    			if(!channelHeartBeatMap.containsKey(ctx.channel())){
    				channelHeartBeatMap.put(ctx.channel(), rpcRequest.getRequestId());
    			}
    			if(heartBeatListener != null){
    				Object[] objs = rpcRequest.getParameters();
        			String versionTemp = "";
        			if(objs != null  && objs.length > 0){
        				versionTemp = (String)objs[0];
        			}
        			InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        			heartBeatListener.channelConnect(versionTemp,insocket.getAddress().getHostAddress(),((RpcRequest) msg).getRequestId());
        		}
    		}
    	}else if(msg instanceof RpcAck){//rpc心跳响应消息
    		RpcAck rpcAck = (RpcAck)msg;
    		if(HeartBeatService.class.getName().equals(rpcAck.getClassName())){//心跳包请求应答
    			unRecPongTimes = 0;
    			isHeartBeatresp = true;
    		}
    	}
    	if(!isHeartBeatresp){
    		super.channelRead(ctx, msg);
    	}
    }
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if(heartBeatListener != null){
			InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
			String heartBeatRequestId = channelHeartBeatMap.get(ctx.channel());
			channelHeartBeatMap.remove(ctx.channel());
			heartBeatListener.channelDisconnect(insocket.getAddress().getHostAddress(),heartBeatRequestId);
		}
    }
	

	
	private void sendHeartAck(Channel channel,String requestId,String className){		
		RpcAck rpcAck = new RpcAck();
		rpcAck.setRequestId(requestId);
		rpcAck.setClassName(className);
		try{
			channel.writeAndFlush(rpcAck);
		}catch(Exception e){
			channel.close();
		}
    }
	
	
	private  RpcRequest getHeartBeatRequest(Class<?> claz){
		Method method = claz.getMethods()[0];
		String className = claz.getName();
		List<String> parameterTypes = new LinkedList<String>();
		for (Class<?> parameterType : method.getParameterTypes()) {
			parameterTypes.add(parameterType.getName());
		}
		if(StringUtil.isNullOrEmpty(heartBeatRequestId)){
			heartBeatRequestId = "MACHINE_"+RpcUtil.generateRequestID();
		}
		Object[] obj = new Object[]{version};
		RpcRequest request = new RpcRequest(heartBeatRequestId, className,
				method.getName(), method.getParameterTypes(), obj);
		return request;
	}
	
}
