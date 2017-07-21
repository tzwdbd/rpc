package com.haihu.rpc.common;

import java.lang.reflect.Method;

public class RpcRequestObj {
	
	private volatile boolean isStart = false;//rpc任务是否开始
	
	private Method method;
	
	private Object[] objs;
	
	private long timeout = System.currentTimeMillis()+Constants.REQUEST_TIMEOUT;//超时时间
	
	public RpcRequestObj(Method method,Object[] objs){
		this.method = method;
		this.objs = objs;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object[] getObjs() {
		return objs;
	}

	public void setObjs(Object[] objs) {
		this.objs = objs;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}
}
