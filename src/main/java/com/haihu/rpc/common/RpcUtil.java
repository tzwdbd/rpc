package com.haihu.rpc.common;

import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.client.RpcNettyClient;
import com.haihu.rpc.server.RpcNettyServer;

public class RpcUtil {
	
	private static Log log = LogFactory.getLog(RpcUtil.class);
	
	public static Object getRemotionServiceByInterface(String name){
    	if(StringUtil.isNullOrEmpty(name)){
    		return null;
    	}
    	Map<String,Object> map = SpringObjectFactory.getInstancesWithAnnotation(RemoteService.class);
    	for (Map.Entry<String, Object> entry : map.entrySet()) {
    		String key = entry.getKey();
      	  	if(!StringUtil.isNullOrEmpty(key)){
	      	  	key = key.toLowerCase();
	      	  	name = name.toLowerCase();
	      	  	if(key.contains(name)){
	      	  		return entry.getValue();
	      	  	}
      	  	}
    	}
    	return null;
    }
	
	public static String generateRequestID() {
		return UUID.randomUUID().toString().replaceAll("-","");
	}
	
	/**
     * 发送rpc执行结果
     * @param result
     * @param requestId
     * @param ipAddress
     */
    public static void sendResult(Map<String, RpcResponseObj> msgRespMap,Object result,String requestId,String ipAddress){
    	RpcResponseObj obj = msgRespMap.get(requestId);
    	if(obj != null){
    		obj.setTimeout(System.currentTimeMillis()+Constants.REQUEST_TIMEOUT);
    	}else{
    		obj = new RpcResponseObj(result,requestId,ipAddress);
    		msgRespMap.put(requestId, obj);
    	}
    	
    	Channel channel = null;
    	try{
    		RpcNettyClient rpcNettyClient = (RpcNettyClient)SpringObjectFactory.getInstance("rpcNettyClient");
    		channel = rpcNettyClient.getChannel();
    	}catch(Exception e){}
    	
    	if(channel == null){
    		try{
    			RpcNettyServer rpcNettyServer = (RpcNettyServer)SpringObjectFactory.getInstance("rpcNettyServer");
        		channel = rpcNettyServer.getChannel(ipAddress);
    		}catch(Exception e){}
    	}
    	
    	if(channel != null){
    		RpcResponse rpcResponse = new RpcResponse();
    		rpcResponse.setRequestId(requestId);
    		rpcResponse.setResult(result);
    		if(channel != null){
    			try{
    				channel.writeAndFlush(rpcResponse);
    			}catch(Exception e){
    				channel.close();
    			}
    		}
    	}
    }
    
    public static Object handle(RpcRequest request) throws Throwable {
    	String className = request.getClassName();
    	String[] classNameSplits = className.split("\\.");
    	String serviceName = classNameSplits[classNameSplits.length - 1];
    	Object bean = RpcUtil.getRemotionServiceByInterface(serviceName);
    	Object result = null;
    	if(bean != null){
    		Method method = bean.getClass().getMethod(request.getMethodName(), request.getTypeParameters());
    		result = method.invoke(bean, request.getParameters());
    	}else{
    		log.error("className:"+className+"can't find bean");
    	}
		return result;
	}
}
