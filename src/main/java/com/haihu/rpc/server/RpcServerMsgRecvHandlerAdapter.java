package com.haihu.rpc.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;

import java.net.InetSocketAddress;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.common.ChannelStatusListener;
import com.haihu.rpc.common.RpcBaseHandlerAdapter;
import com.haihu.rpc.common.RpcRequest;
import com.haihu.rpc.common.RpcUtil;

@Sharable
public class RpcServerMsgRecvHandlerAdapter extends RpcBaseHandlerAdapter {
	
	private Log log = LogFactory.getLog(getClass());
    private ChannelGroup channelGroup = new DefaultChannelGroup ("recv", GlobalEventExecutor.INSTANCE);
    
    @Override
    public void closeMore(){
    	if(channelGroup != null){
    		try{
    			channelGroup.close();
    		}catch(Throwable e){
    			log.error("MessageRecvHandlerAdapter.close",e);
    		}
    	}
    }
    
    public RpcServerMsgRecvHandlerAdapter(ChannelStatusListener listener){
    	super(listener);
    }
    
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if(channelGroup != null){
        	channelGroup.add(ctx.channel());
        }
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        ctx.channel().pipeline().addFirst(sslCtx.newHandler(ctx.channel().alloc()));
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if(channelGroup != null){
			channelGroup.remove(ctx.channel());
		}
    }
    
    @Override
    public Channel getChannel(String ipAddress){
    	if(StringUtil.isNullOrEmpty(ipAddress)){
    		return null;
    	}
    	Iterator<Channel> iterator = channelGroup.iterator();
    	while(iterator.hasNext()){
    		Channel channel = iterator.next();
    		if(channel != null){
    			InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
//        		if(ipAddress.equals(insocket.getHostName())){
//        			return channel;
//        		}
        		if(ipAddress.equals(insocket.getAddress().getHostAddress())){
        			return channel;
        		}
    		}
    	}
    	
    	return null;
    }

    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	super.channelRead(ctx, msg);
    	if(msg instanceof RpcRequest){//rpc请求消息
    		final RpcRequest rpcRequest = (RpcRequest) msg;
    		sendAck(ctx.channel(),(byte)1, (byte)1, rpcRequest.getRequestId());
    		fixedThreadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Object result = null;
					try {
						result = RpcUtil.handle(rpcRequest);
					} catch (Throwable e) {
						// TODO Auto-generated catch block
						log.error(e);
						result = null;
					}
					String ipAddress = "";
					InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
					if(insocket != null){
//						ipAddress = insocket.getHostName();
						ipAddress = insocket.getAddress().getHostAddress();
					}
					if(result != null){
						RpcUtil.sendResult(msgRespMap,result, rpcRequest.getRequestId(),ipAddress);
					}
				}
			});
    	}
    }
}
