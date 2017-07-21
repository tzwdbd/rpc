package com.haihu.rpc.common;

import java.lang.reflect.Method;


public interface RpcCallback {
	/**
	 * ack消息发送回调,禁止在该函数做长时间操作
	 * 如果要做长时间操作请另起线程
	 * @param isSuccess
	 * @param method
	 * @param objs
	 */
	public void callbackAck(boolean isSuccess,Method method,Object[] objs);
	
	
	/**
	 * rpc执行结果回调
	 * @param result
	 * @param method
	 * @param objs
	 */
	public void callbackResult(Object result,Method method,Object[] objs);

}
