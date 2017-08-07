package com.haihu.rpc.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RpcBaseHandlerAdapter extends ChannelInboundHandlerAdapter {
	private static Log log = LogFactory.getLog(RpcBaseHandlerAdapter.class);
	protected static Map<String, RpcResponseObj> msgRespMap = new ConcurrentHashMap<String, RpcResponseObj>();
	protected static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(8);
	protected static List<FutureTaskImpl<Object>> futureList = new ArrayList<FutureTaskImpl<Object>>();
	protected static ScheduledExecutorService executorService = null;
	protected static List<RpcInvoker> invokerList = new Vector<RpcInvoker>();
	
	protected ChannelStatusListener channelStatusListener;
	
    public RpcBaseHandlerAdapter(ChannelStatusListener channelStatusListener){
    	this.channelStatusListener = channelStatusListener;
    	timeoutCheck();
    }
    
    public void close(){
    	closeMore();
    }
    
    public void addRpcInvoker(RpcInvoker invoker,String ipAddress){
    	if(invoker != null){
    		Channel channel = getChannel(ipAddress);
    		if(channel != null){
    			invoker.setChannel(channel);
    			invokerList.add(invoker);
    		}
    	}
    }
    
    protected void doCallbackAck(String requestId,boolean isSuccess){
    	for(RpcInvoker invoker : invokerList){
    		if(invoker.doGetCallbackAck(requestId,isSuccess)){
    			break;
    		}
    	}
    }
	
	protected void doCallbackResult(String requestId,Object result){
    	for(RpcInvoker invoker : invokerList){
    		if(invoker.doCallbackResult(result, requestId)){
    			break;
    		}
    	}
    }
    
    /**
     * 超时检测
     */
    private static void timeoutCheck(){
    	if(executorService == null){
    		executorService = Executors.newScheduledThreadPool(1);
    		executorService.scheduleAtFixedRate(new Runnable() {
    			
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				timeoutCheckTask();//20分钟任务超时
    				timeoutCheckResp();//15秒结果回传超时
    				timeoutCheckAck();//15秒ack包超时
    			}
    		}, 1, 4, TimeUnit.SECONDS);
    	}
    }
    
    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	if(msg instanceof RpcResponse){//rpc执行结果
    		final RpcResponse rpcResponse = (RpcResponse)msg;
    		sendAck(ctx.channel(),(byte)2,(byte)1, rpcResponse.getRequestId());
    		
    		fixedThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					doCallbackResult(rpcResponse.getRequestId(), rpcResponse.getResult());
				}
			});
    	}else if(msg instanceof RpcAck){//rpc应答包
    		final RpcAck rpcAck = (RpcAck)msg;
    		if(rpcAck.getType() == 1){
    			fixedThreadPool.execute(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(rpcAck.getStatus() == 1){
							doCallbackAck(rpcAck.getRequestId(),true);
						}else{
							doCallbackAck(rpcAck.getRequestId(),false);
						}
					}
				});
    		}else if(rpcAck.getType() == 2){
    			synchronized (msgRespMap) {
    				msgRespMap.remove(rpcAck.getRequestId());
				}
    		}
    	}
    }
    
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
//        log.error("channelActive = "+insocket.getHostName());
        log.error("channelActive-- = "+insocket.getAddress().getHostAddress());
        if(channelStatusListener != null){
			channelStatusListener.channelConnect(ctx.channel());
		}
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        log.error("channelInactive = "+insocket.getAddress().getHostAddress());
		if(channelStatusListener != null){
			channelStatusListener.channelDisconnect(ctx.channel());
		}
    }
    
	/**
	 * 检测rpc请求超时
	 */
    private static void timeoutCheckAck(){
    	List<RpcInvoker> cleanInvokerList = new ArrayList<RpcInvoker>();
		if(invokerList != null && invokerList.size() > 0){
			for(RpcInvoker invoker : invokerList){
				invoker.timeoutCheck();
				if(invoker.getMsgMap().size() == 0){
					cleanInvokerList.add(invoker);
				}
			}
			//清理
			if(cleanInvokerList.size() > 0){
				synchronized (invokerList) {
					invokerList.removeAll(cleanInvokerList);
				}
				cleanInvokerList.clear();
			}
			cleanInvokerList = null;
		}
    }
    
    /**
     * 检测执行结果回传超时
     */
    public static void timeoutCheckResp(){
    	synchronized (msgRespMap) {
    		for (Map.Entry<String, RpcResponseObj> entry : msgRespMap.entrySet()) {  
    			RpcResponseObj obj = entry.getValue();
    			if(obj.getTimeout() <= System.currentTimeMillis()){//超时就重新发送
    				log.error("超时重新发送timeoutCheckResp");
    				RpcUtil.sendResult(msgRespMap,obj.getResult(), obj.getRequestId(),obj.getIpAddress());
    			}
    		}
		}
    }
    
    
    /**
     * 发送应答
     * @param channel
     * @param requestId
     */
    protected void sendAck(Channel channel,byte type,byte status,String requestId){
    	RpcAck rpcAck = new RpcAck();
    	rpcAck.setType(type);
    	rpcAck.setStatus(status);
    	rpcAck.setRequestId(requestId);
    	try{
    		channel.writeAndFlush(rpcAck);
    	}catch(Exception e){
    		channel.close();
    	}
    }
    
    public Channel getChannel(String ipAddress){
    	return null;
    }
    
    public void closeMore(){}
    
    /**
     * 20分钟客户端任务超时检测
     */
    public static void timeoutCheckTask(){
    	List<FutureTaskImpl<Object>> list = new ArrayList<FutureTaskImpl<Object>>();
    	synchronized (futureList) {
    		for(FutureTaskImpl<Object> f : futureList){
        		if(f.isDone()){
        			list.add(f);
        		}else{
        			if(f.getTimeout() <= System.currentTimeMillis()){//超时就取消任务
        				log.error("timeoutCheckTask有超时任务");
        				list.add(f);
        				
        				try{
        					f.cancel(true);
        				}catch(Exception e){
        					log.error(e);
        				}
        				
        				//杀掉浏览器进程
        				try {
        					log.debug("--->开始任务");
        					Runtime.getRuntime().exec("taskkill -f -im firefox.exe");
        				} catch (Exception e) {
        					// TODO Auto-generated catch block
        					log.error("结束任务异常",e);
        				}
            		}
        		}
        	}
        	if(list.size() > 0){
        		futureList.removeAll(list);
        	}
		}
    }
    
}
