package com.barchart.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelStateHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * HTTP server core.
 */
public class HttpServer {

	private Channel serverChannel;
	private HttpServerConfig config;
	private HttpRequestChannelHandler channelHandler;
	private ConnectionTracker clientTracker;

	private final ChannelGroup channelGroup = new DefaultChannelGroup();

	public HttpServer configure(final HttpServerConfig config_) {

		config = config_;
		channelHandler = new HttpRequestChannelHandler(config);
		clientTracker = new ConnectionTracker(config.maxConnections());

		return this;

	}

	/**
	 * Start the server with the configuration settings provided.
	 */
	public ChannelFuture listen() {

		if (config == null) {
			throw new IllegalStateException("Server has not been configured");
		}

		if (serverChannel != null) {
			throw new IllegalStateException("Server is already running.");
		}

		final ChannelFuture future = new ServerBootstrap() //
				.group(config.parentGroup(), config.childGroup()) //
				.channel(NioServerSocketChannel.class) //
				.localAddress(config.address()) //
				.childHandler(new HttpServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144) //
				.bind();

		serverChannel = future.channel();

		return future;

	}

	/**
	 * Shutdown the server. This does not kill active client connections.
	 */
	public ChannelFuture shutdown() {

		if (serverChannel == null) {
			throw new IllegalStateException("Server is not running.");
		}

		final ChannelFuture future = serverChannel.close();
		serverChannel = null;

		return future;

	}

	/**
	 * Return a future for the server shutdown process.
	 */
	public ChannelFuture shutdownFuture() {
		return serverChannel.closeFuture();
	}

	/**
	 * Shutdown the server and kill all active client connections.
	 */
	public ChannelGroupFuture kill() {

		if (serverChannel == null) {
			throw new IllegalStateException("Server is not running.");
		}

		channelGroup.add(serverChannel);
		final ChannelGroupFuture future = channelGroup.close();
		channelGroup.remove(serverChannel);
		serverChannel = null;

		return future;

	}

	public boolean isRunning() {
		return serverChannel != null;
	}

	public HttpServerConfig config() {
		return config;
	}

	private class HttpServerChannelInitializer extends
			ChannelInitializer<SocketChannel> {

		@Override
		public void initChannel(final SocketChannel ch) throws Exception {

			final ChannelPipeline pipeline = ch.pipeline();

			pipeline.addLast(new HttpResponseEncoder(), //
					clientTracker, //
					new HttpRequestDecoder(), //
					new HttpChunkAggregator(65536), //
					// new MessageLoggingHandler(LogLevel.INFO), //
					channelHandler);

		}

	}

	@Sharable
	private class ConnectionTracker extends ChannelStateHandlerAdapter {

		private int maxConnections = -1;

		public ConnectionTracker(final int connections) {
			maxConnections = connections;
		}

		@Override
		public void channelActive(final ChannelHandlerContext context) {

			if (maxConnections > -1 && channelGroup.size() >= maxConnections) {

				final ByteBuf content = Unpooled.buffer();
				content.writeBytes("503 Service Unavailable - Server Too Busy"
						.getBytes());

				final HttpResponse response =
						new DefaultHttpResponse(HttpVersion.HTTP_1_1,
								HttpResponseStatus.SERVICE_UNAVAILABLE);
				response.setContent(content);
				response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
						content.readableBytes());

				context.write(response)
						.addListener(ChannelFutureListener.CLOSE);

				return;

			}

			channelGroup.add(context.channel());
			context.fireChannelActive();

		}

		@Override
		public void channelInactive(final ChannelHandlerContext context) {

			channelGroup.remove(context.channel());
			context.fireChannelInactive();

		}

	}

}
