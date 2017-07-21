package com.haihu.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;

public class RpcMessageEncoder extends MessageToByteEncoder<Object> {
	
	@Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encode(baos, msg);
		byte[] data = baos.toByteArray();
		out.writeBytes(data);
    }
	
	@SuppressWarnings("unchecked")
	private void encode(OutputStream out, Object object) throws IOException {
		LinkedBuffer buffer = BufferCache.getBuffer();
		Schema<Object> schema = (Schema<Object>)SchemaCache.getSchema(object.getClass());
		int length = ProtostuffIOUtil.writeTo(buffer, object, schema);
		IOUtils.writeInt(out, length+1);
		
		//消息类型 1:RpcRequest  2:RpcResponse 3:RpcAck
		int type = MsgType.getValueByClaz(object.getClass());
		IOUtils.writeByte(out, (byte)type);
		LinkedBuffer.writeTo(out, buffer);
	}
}
