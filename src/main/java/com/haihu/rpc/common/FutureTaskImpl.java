package com.haihu.rpc.common;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureTaskImpl<T> extends FutureTask<T>{
	
	private long timeout = System.currentTimeMillis()+20*60*1000;//20分钟任务超时时间

	public FutureTaskImpl(Callable<T> callable) {
		super(callable);
		// TODO Auto-generated constructor stub
	}

	public long getTimeout() {
		return timeout;
	}
}
