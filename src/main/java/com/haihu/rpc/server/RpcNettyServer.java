package com.haihu.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.haihu.rpc.common.Constants;
import com.haihu.rpc.common.HeartBeatHandlerAdapter;
import com.haihu.rpc.common.HeartBeatListener;
import com.haihu.rpc.common.RpcInvoker;
import com.haihu.rpc.common.RpcMessageDecoder;
import com.haihu.rpc.common.RpcMessageEncoder;

public class RpcNettyServer implements HeartBeatListener{
	Logger log = Logger.getLogger(getClass());
	// 端口
	private int port;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	ChannelFuture channelFuture;

	ServerBootstrap bootstrap;
	
	private volatile RpcServerInitializer rpcServerInitializer;
	
	private HeartBeatListener heartBeatListener;
	
	public void addRpcInvoker(RpcInvoker invoker,String ipAddress){
		if(rpcServerInitializer != null){
			rpcServerInitializer.addRpcInvoker(invoker, ipAddress);
		}
	}

	public void startServer() {
		new Thread(){
			public void run(){
		        //方法返回到Java虚拟机的可用的处理器数量
		        EventLoopGroup bossGroup = new NioEventLoopGroup(); 
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
				try {
					if(rpcServerInitializer != null){
						rpcServerInitializer.close();
						rpcServerInitializer = null;
					}
					rpcServerInitializer = new RpcServerInitializer();
					bootstrap = new ServerBootstrap();
					bootstrap.group(bossGroup, workerGroup);
					bootstrap.channel(NioServerSocketChannel.class);
					bootstrap.childHandler(rpcServerInitializer);
					bootstrap.option(ChannelOption.SO_BACKLOG, 128);
		            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
					// 服务器绑定端口监听
					channelFuture = bootstrap.bind(port).sync();
					// 监听服务器关闭监听，此方法会阻塞
					if(channelFuture.isSuccess()){
						log.error("server start success!");
					}
					channelFuture.channel().closeFuture().sync();
					// 可以简写为
					/* b.bind(portNumber).sync().channel().closeFuture().sync(); */
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();
				}
			}
		}.start();
	}

	private class RpcServerInitializer extends
			ChannelInitializer<SocketChannel> {
		
		private volatile RpcServerMsgRecvHandlerAdapter rpcServerMsgRecvSHandlerAdapter;
		private volatile HeartBeatHandlerAdapter heartBeatHandlerAdapter;
		
		public RpcServerInitializer(){
			rpcServerMsgRecvSHandlerAdapter = new RpcServerMsgRecvHandlerAdapter(null);
			heartBeatHandlerAdapter = new HeartBeatHandlerAdapter();
			heartBeatHandlerAdapter.setHeartBeatListener(RpcNettyServer.this);
		}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast("decoder",new RpcMessageDecoder());
			pipeline.addLast("encoder", new RpcMessageEncoder());
			pipeline.addLast("pong", new IdleStateHandler(Constants.HEART_READ_TIMEOUT, 0, 0,TimeUnit.SECONDS));
			pipeline.addLast("heartbeat", heartBeatHandlerAdapter);
			pipeline.addLast("handler", rpcServerMsgRecvSHandlerAdapter);
		}
		
		public void addRpcInvoker(RpcInvoker invoker,String ipAddress){
			if(rpcServerMsgRecvSHandlerAdapter != null){
				rpcServerMsgRecvSHandlerAdapter.addRpcInvoker(invoker,ipAddress);
			}
		}
		public void close(){
			if(rpcServerMsgRecvSHandlerAdapter != null){
				rpcServerMsgRecvSHandlerAdapter.close();
			}
		}
		
		public Channel getChannel(String ipAddress){
			if(rpcServerMsgRecvSHandlerAdapter != null){
				return rpcServerMsgRecvSHandlerAdapter.getChannel(ipAddress);
			}
			return null;
		}
	}

	public void stopServer() {
		if (channelFuture != null) {
			channelFuture.channel().close();
		}
	}

	public HeartBeatListener getHeartBeatListener() {
		return heartBeatListener;
	}

	public void setHeartBeatListener(HeartBeatListener heartBeatListener) {
		this.heartBeatListener = heartBeatListener;
	}
	
	public Channel getChannel(String ipAddress){
		if(rpcServerInitializer != null){
			return rpcServerInitializer.getChannel(ipAddress);
		}
		return null;
	}

	@Override
	public void channelDisconnect(String ip,String heartBeartRequestId) {
		// TODO Auto-generated method stub
		if(heartBeatListener != null){
			heartBeatListener.channelDisconnect(ip,heartBeartRequestId);
		}
	}

	@Override
	public void channelConnect(String version, String ip,String heartBeartRequestId) {
		// TODO Auto-generated method stub
		if(heartBeatListener != null){
			heartBeatListener.channelConnect(version, ip,heartBeartRequestId);
		}
	}
}
