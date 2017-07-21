package com.haihu.rpc.common;


/**
 * rpc执行结果回传结果包
 * @author boliu
 *
 */

public class RpcResponse{

	private String requestId;

	private Throwable exception;

	private Object result;
	

	public RpcResponse() {
	}

	public RpcResponse(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
