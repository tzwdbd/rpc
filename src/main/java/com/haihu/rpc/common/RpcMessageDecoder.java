package com.haihu.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class RpcMessageDecoder extends ByteToMessageDecoder {
	private  Log log = LogFactory.getLog(getClass());
	

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// 网络有异常要关闭通道
		ctx.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		in.markReaderIndex();
		//判断缓冲区是否可读
		if (!in.isReadable()) {
			in.resetReaderIndex();
        	return;
		}
		if (in.readableBytes() < 4) {  //这个HEAD_LENGTH的长度
			in.resetReaderIndex();
			return;
        }
        int dataLength = in.readInt(); // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        if (in.readableBytes() < dataLength) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex
            in.resetReaderIndex();
            return ;
        }

        byte type = in.readByte(); //读取消息类型 1:RpcRequest  2:RpcResponse 3:RpcAck
        Class<?> claz = MsgType.getClazByValue(type);
        if(claz != null){
        	byte[] data = new byte[dataLength-1];//嗯，这时候，我们读到的长度，满足我们的要求了，把传送过来的数据，取出来吧~~
            in.readBytes(data);
            Schema<Object> schemaRes = (Schema<Object>)RuntimeSchema.getSchema(claz); 
    		try{
    			Object message = (Object)schemaRes.newMessage();
    			ProtostuffIOUtil.mergeFrom(data, message, schemaRes);
    	        out.add(message);
    		}catch(Exception e){
    			log.error(e);
    		}
        }else{
        	log.error("找不到该类型的消息type = "+type);
        }
	}
}
