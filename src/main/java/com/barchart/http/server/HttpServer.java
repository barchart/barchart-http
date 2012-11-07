package com.barchart.http.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.SocketAddress;

import com.barchart.http.api.RequestHandler;
import com.barchart.http.api.RequestHandlerFactory;

/**
 * HTTP server core.
 */
public class HttpServer {

	private final DefaultURLMapper mapper = new DefaultURLMapper();
	private final HttpRequestChannelHandler channelHandler =
			new HttpRequestChannelHandler(mapper);

	private Channel channel;

	/**
	 * Start the server on the specified address.
	 */
	public ChannelFuture listen(final SocketAddress address) {
		return listen(address, new NioEventLoopGroup(), new NioEventLoopGroup());
	}

	/**
	 * Start the server on the specified address, overriding the event loop
	 * groups used by Netty.
	 */
	public ChannelFuture listen(final SocketAddress address,
			final EventLoopGroup parent, final EventLoopGroup child) {

		final ChannelFuture future = new ServerBootstrap() //
				.group(parent, child) //
				.channel(NioServerSocketChannel.class) //
				.localAddress(address) //
				.childHandler(new HttpServerChannelInitializer()) //
				.option(ChannelOption.SO_REUSEADDR, true) //
				.option(ChannelOption.SO_SNDBUF, 262144) //
				.option(ChannelOption.SO_RCVBUF, 262144) //
				.bind();

		channel = future.channel();

		return future;

	}

	/**
	 * Shutdown the server. This does not kill active client connections.
	 */
	public ChannelFuture shutdown() {
		return channel.close();
	}

	/**
	 * Shutdown the server and kill all active client connections.
	 */
	public ChannelFuture kill() {
		// TODO: kill all client connections
		return shutdown();
	}

	/**
	 * Add a request handler for the given path.
	 */
	public HttpServer addHandler(final String path, final RequestHandler handler) {
		mapper.addHandler(path, handler);
		return this;
	}

	/**
	 * Add a request handler factory for the given path.
	 */
	public HttpServer addHandler(final String path,
			final RequestHandlerFactory factory) {
		mapper.addHandler(path, factory);
		return this;
	}

	private class HttpServerChannelInitializer extends
			ChannelInitializer<SocketChannel> {

		@Override
		public void initChannel(final SocketChannel ch) throws Exception {

			final ChannelPipeline pipeline = ch.pipeline();

			pipeline.addLast(new HttpRequestDecoder(), //
					new HttpChunkAggregator(65536), //
					new HttpResponseEncoder(), //
					// new MessageLoggingHandler(LogLevel.INFO), //
					channelHandler);

		}

	}

}
