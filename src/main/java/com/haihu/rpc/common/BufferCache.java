package com.haihu.rpc.common;

import com.dyuproject.protostuff.LinkedBuffer;

public class BufferCache {

	private static ThreadLocal<LinkedBuffer> BUFFERS = new ThreadLocal<LinkedBuffer>() {
		protected LinkedBuffer initialValue() {
			return LinkedBuffer.allocate(4096);
		};
	};

	public static LinkedBuffer getBuffer() {
		LinkedBuffer buffer = BUFFERS.get();
		buffer.clear();
		return buffer;
	}
}
