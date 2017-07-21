package com.haihu.rpc.common;


/**
 * 应答包
 * @author boliu
 *
 */

public class RpcAck{
	
	private String requestId;
	
	private String className;
	
	private byte type;//1:RpcRequest ack包 2:RpcResponse ack包
	
	private byte status;//1:accept 2:reject

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

}
