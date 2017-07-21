package com.haihu.rpc.common;

import java.lang.reflect.Method;

public class MethodObj {
	
	private Method method;
	
	private Object[] objs;
	
	private long timeout = System.currentTimeMillis()+15000;//超时时间
	
	public MethodObj(Method method,Object[] objs){
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
}
