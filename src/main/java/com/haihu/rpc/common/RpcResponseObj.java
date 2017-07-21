package com.haihu.rpc.common;


public class RpcResponseObj {
	private String requestId;
	private Object result;
	private String ipAddress;
	private long timeout = System.currentTimeMillis()+15000;//超时时间
	
	public RpcResponseObj(Object result,String requestId){
		this.result = result;
		this.requestId = requestId;
	}
	
	public RpcResponseObj(Object result,String requestId,String ipAddress){
		this.result = result;
		this.requestId = requestId;
		this.ipAddress = ipAddress;
	}
	
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	
}
