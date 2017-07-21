package com.haihu.rpc.common;

/**
 * rpc请求包
 * @author boliu
 *
 */

public class RpcRequest{

	private String requestId;

    private String className;
    private String methodName;
    private Class<?>[] typeParameters;
    private Object[] parametersVal;
    
    public RpcRequest(){}
    
    public RpcRequest(String requestID, String className, String methodName,
    		Class<?>[] parameterTypes, Object[] parameters) {
		this.requestId = requestID;
		this.className = className;
		this.methodName = methodName;
		this.typeParameters = parameterTypes;
		this.parametersVal = parameters;
	}
    
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(Class<?>[] typeParameters) {
        this.typeParameters = typeParameters;
    }

    public Object[] getParameters() {
        return parametersVal;
    }

    public void setParameters(Object[] parametersVal) {
        this.parametersVal = parametersVal;
    }

}
