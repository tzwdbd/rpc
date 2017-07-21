package com.haihu.rpc.common;

import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class RpcInvoker implements InvocationHandler {
	protected static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
	private Logger log = Logger.getLogger(getClass());
	private Channel channel;
	private Map<String, RpcRequestObj> msgMap = new ConcurrentHashMap<String, RpcRequestObj>();
	private RpcCallback callback;
	
	public RpcInvoker(RpcCallback callback) {
		// TODO Auto-generated constructor stub
		this.callback = callback;
	}
	
	public Map<String, RpcRequestObj> getMsgMap(){
		return msgMap;
	}
	
	public RpcCallback getRpcCallback(){
		return callback;
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public synchronized void timeoutCheck(){
		List<String> cleanList = new ArrayList<String>();
		for (Map.Entry<String, RpcRequestObj> entry : msgMap.entrySet()) {  
			RpcRequestObj methodObj = entry.getValue();
			if(!methodObj.isStart() && methodObj.getTimeout() <= System.currentTimeMillis()){//已经超时
				cleanList.add(entry.getKey());
				//超时回调
				doCallbackAck(false,methodObj);
			}
		}
		//清理
		if(cleanList.size() > 0){
			for(String key : cleanList){
				msgMap.remove(key);
			}
			cleanList.clear();
		}
		cleanList = null;
	}
	
	//ack成功回调 不需要移除msgMap requestId,result回调才需要移除
	public synchronized boolean doGetCallbackAck(String requestId,boolean isSuccess){
		RpcRequestObj methodObj = msgMap.get(requestId);
		if(methodObj != null){
			if(isSuccess){//任务开始执行,返回成功
				methodObj.setStart(true);
			}else{//没有获取到任务执行的锁,返回失败
				msgMap.remove(requestId);
			}
			doCallbackAck(isSuccess, methodObj);
			return true;
		}
		return false;
	}
	
	//执行结果成功回调
	public synchronized boolean doCallbackResult(Object result,String requestId){
		RpcRequestObj methodObj = msgMap.get(requestId);
		if(methodObj != null){
			msgMap.remove(requestId);
			doCallbackResult(result,methodObj);
			return true;
		}
		return false;
	}
	
	private void doCallbackResult(final Object result,final RpcRequestObj methodObj){
		if(callback != null){
			fixedThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						callback.callbackResult(result, methodObj.getMethod(), methodObj.getObjs());
					}catch(Throwable e){
						log.error(e);
					}
				}
			});
			
		}
	}
	
	private void doCallbackAck(final boolean isSuccess,final RpcRequestObj methodObj){
		if(callback != null){
			fixedThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						callback.callbackAck(isSuccess, methodObj.getMethod(), methodObj.getObjs());
					}catch(Throwable e){
						log.error(e);
					}
				}
			});
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		if(channel == null){
			log.error("invoke channel is null");
			return null;
		}
		String className = method.getDeclaringClass().getName();
		List<String> parameterTypes = new LinkedList<String>();
		for (Class<?> parameterType : method.getParameterTypes()) {
			parameterTypes.add(parameterType.getName());
		}

		String requestID = RpcUtil.generateRequestID();
		msgMap.put(requestID, new RpcRequestObj(method,args));
		RpcRequest rpcRequest = new RpcRequest(requestID, className,
				method.getName(), method.getParameterTypes(), args);
		try {
			channel.writeAndFlush(rpcRequest);
		} catch (Throwable t) {
			channel.close();
			log.error("rpcinvoker write",t);
		}
		return null;
	}
}
