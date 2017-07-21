package com.haihu.rpc.common;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CallableImpl implements Callable<Object>{
	private static Log log = LogFactory.getLog(CallableImpl.class);
	private RpcRequest rpcRequest;
	private Semaphore lock;
	private Map<String, RpcResponseObj> msgRespMap = null;
	
	public CallableImpl(RpcRequest rpcRequest,Semaphore lock,Map<String, RpcResponseObj> msgRespMap){
		this.rpcRequest = rpcRequest;
		this.lock = lock;
		this.msgRespMap = msgRespMap;
	}

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		Object result = null;
		try {
			result = RpcUtil.handle(rpcRequest);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			result = null;
			log.error("call exception = ",e);
		}finally{
			try{
				if(result != null){
					log.error("begin send");
					RpcUtil.sendResult(msgRespMap,result, rpcRequest.getRequestId(),null);
					Thread.sleep(10000);
				}
			}catch(Throwable e){
				log.error(e);
			}
			log.error("begin send end");
			lock.release();
		}
		return result;
	}
	
}