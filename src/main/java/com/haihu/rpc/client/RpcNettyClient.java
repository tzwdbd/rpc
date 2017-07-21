package com.haihu.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.haihu.rpc.common.ChannelStatusListener;
import com.haihu.rpc.common.Constants;
import com.haihu.rpc.common.HeartBeatHandlerAdapter;
import com.haihu.rpc.common.RpcInvoker;
import com.haihu.rpc.common.RpcMessageDecoder;
import com.haihu.rpc.common.RpcMessageEncoder;

public class RpcNettyClient implements ChannelStatusListener{
	private  Log log = LogFactory.getLog(getClass());
	private String host ;
	private int port ;
	private ScheduledExecutorService executorService ;
	private volatile RpcClientInitializer rpcClientInitializer;
	private String version;
	private EventLoopGroup group;
	private SslContext sslContext;
	
	public RpcNettyClient(){
		try {
//			sslContext = SslContextBuilder.forClient().sslProvider(SslProvider.OPENSSL).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			sslContext = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
        public void operationComplete(ChannelFuture f) throws Exception {
            if (f.isSuccess()) {
            	log.error("connect server success");
            } else {
            	log.error( "connect server fail,reconnecting");
            	try{
            		f.channel().close();
            	}catch(Exception e){}
            	connServer();
            }
        }
    };

	public void start() {
		connServer();
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void addRpcInvoker(RpcInvoker invoker){
		if(rpcClientInitializer != null){
			rpcClientInitializer.addRpcInvoker(invoker);
		}
	}
	
	public boolean isBusy(){
		if(rpcClientInitializer != null){
			return rpcClientInitializer.isBusy();
		}
		return false;
	}

	private void connServer(){
		int deplay = Constants.RE_CONN_WAIT_SECONDS;
		if(executorService == null){
			deplay = 1;
			executorService = Executors.newScheduledThreadPool(1);
		}
		
		if(executorService.isShutdown()){
			log.error("程序停止中,重连的线程停止");
			return;
		}
		if(group != null){
			group.shutdownGracefully();
			group = null;
		}
	    group = new NioEventLoopGroup(); 
		final Bootstrap bootstrap = new Bootstrap();
		
		if(rpcClientInitializer != null){
			rpcClientInitializer.close();
			rpcClientInitializer = null;
		}
		rpcClientInitializer = new RpcClientInitializer();
		rpcClientInitializer.setVersion(version);
		bootstrap.group(group).channel(NioSocketChannel.class).handler(rpcClientInitializer);
		
		executorService.schedule(new Runnable() {
			public void run() {
				try {
					log.error("connect to "+host+":"+port);
					ChannelFuture future = bootstrap.connect(host, port);
					future.addListener(channelFutureListener);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}, deplay, TimeUnit.SECONDS);
	}
	
	
	public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
		
		private volatile RpcClientMsgRecvHandlerAdapter rpcClientMsgRecvHandlerAdapter;
		private volatile HeartBeatHandlerAdapter heartBeatHandlerAdapter;
		
		public RpcClientInitializer(){
			rpcClientMsgRecvHandlerAdapter = new RpcClientMsgRecvHandlerAdapter(RpcNettyClient.this);
			heartBeatHandlerAdapter = new HeartBeatHandlerAdapter();
		}
		
		public RpcClientMsgRecvHandlerAdapter getRpcClientMsgRecvHandlerAdapter(){
			return rpcClientMsgRecvHandlerAdapter;
		}
		 
	    @Override
	    protected void initChannel(SocketChannel ch) throws Exception {
	        ChannelPipeline pipeline = ch.pipeline();
	        if (sslContext != null) {
	        	pipeline.addLast(sslContext.newHandler(ch.alloc(), host, port));
            }
	        pipeline.addLast("decoder", new RpcMessageDecoder());
	        pipeline.addLast("encoder", new RpcMessageEncoder());
	        pipeline.addLast("ping", new IdleStateHandler(0, Constants.HEART_WRITE_TIMEOUT, 0,TimeUnit.SECONDS));
	        pipeline.addLast("heartbeat", heartBeatHandlerAdapter);
	        pipeline.addLast("handler", rpcClientMsgRecvHandlerAdapter);
	    }
	    
	    public void addRpcInvoker(RpcInvoker invoker){
	    	if(rpcClientMsgRecvHandlerAdapter != null){
	    		rpcClientMsgRecvHandlerAdapter.addRpcInvoker(invoker,null);
	    	}
	    }
	    
	    public boolean isBusy(){
	    	if(rpcClientMsgRecvHandlerAdapter != null){
	    		return rpcClientMsgRecvHandlerAdapter.isBusy();
	    	}
	    	return false;
	    }
	    
	    public void close(){
	    	if(rpcClientMsgRecvHandlerAdapter != null){
	    		rpcClientMsgRecvHandlerAdapter.close();
	    	}
	    }
	    
	    public void setVersion(String version){
	    	if(heartBeatHandlerAdapter != null){
	    		heartBeatHandlerAdapter.setVersion(version);
	    	}
	    }
	    
	    public Channel getChannel(){
	    	if(rpcClientMsgRecvHandlerAdapter != null){
	    		return rpcClientMsgRecvHandlerAdapter.getChannel(null);
	    	}
	    	return null;
	    }
	}
	
	public void setVersion(String version){
		this.version = version;
		if(rpcClientInitializer != null){
			rpcClientInitializer.setVersion(version);
		}
	}
	
	
	public void stop(){
		if(executorService!=null){
			executorService.shutdown();
		}
		
		if(rpcClientInitializer != null){
			rpcClientInitializer.close();
		}
	}

	@Override
	public void channelDisconnect(Channel channel) {
		// TODO Auto-generated method stub
		try{
			channel.close();
		}catch(Exception e){}
		connServer();
	}

	@Override
	public void channelConnect(Channel channel) {
		// TODO Auto-generated method stub
		
	}
	
	public Channel getChannel(){
		if(rpcClientInitializer != null){
			return rpcClientInitializer.getChannel();
		}
		return null;
	}
}
