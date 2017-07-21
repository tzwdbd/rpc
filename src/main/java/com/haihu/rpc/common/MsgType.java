package com.haihu.rpc.common;

public enum MsgType {
//	1:RpcRequest  2:RpcResponse 3:RpcAck
	RPC_REQUEST(1, RpcRequest.class),
	RPC_RESPONSE(2, RpcResponse.class),
	RPC_ACK(3, RpcAck.class);

    private int value;
    private Class<?> claz;
    
    
    MsgType(int value,Class<?> claz){
    	this.value = value;
    	this.claz = claz;
    }


	public int getValue() {
		return value;
	}


	public void setValue(int value) {
		this.value = value;
	}


	public Class<?> getClaz() {
		return claz;
	}


	public void setClaz(Class<?> claz) {
		this.claz = claz;
	}


	
	public static Class<?> getClazByValue(int value){
		for (MsgType e : MsgType.values()) {  
		    if(e.getValue() == value){ 
		    	return e.getClaz();
		    }
		} 
		return null;
	}
	
	public static int getValueByClaz(Class<?> claz){
		for (MsgType e : MsgType.values()) {  
		    if(e.getClaz().equals(claz)){ 
		    	return e.getValue();
		    }
		} 
		return 0;
	}
	
}
