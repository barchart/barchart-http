/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.RequestHandlerMapping;

/**
 * Netty channel handler for routing inbound requests to the proper
 * RequestHandler.
 */
@Sharable
public class HttpRequestChannelHandler extends
		ChannelInboundMessageHandlerAdapter<FullHttpRequest> {

	public static final AttributeKey<PooledServerResponse> ATTR_RESPONSE =
			new AttributeKey<PooledServerResponse>("response");

	private final HttpServerConfig config;
	private final ServerMessagePool messagePool;

	public HttpRequestChannelHandler(final HttpServerConfig config_) {
		super();
		config = config_;
		messagePool = new ServerMessagePool(config.maxConnections());
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final FullHttpRequest msg) throws Exception {

		// Create request handler
		final RequestHandlerMapping mapping =
				config.getRequestMapping(msg.getUri());

		if (mapping == null) {
			sendNotFound(ctx, msg);
			return;
		}

		final String relativePath =
				msg.getUri().substring(mapping.path().length());

		// Create request/response
		final PooledServerRequest request = messagePool.getRequest();
		request.init(ctx.channel(), msg, relativePath);

		final RequestHandler handler = mapping.handler(request);

		final PooledServerResponse response = messagePool.getResponse();
		response.init(ctx, this, handler, request, config.logger());

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_RESPONSE).set(response);

		try {

			// Process request
			handler.onRequest(request, response);

		} catch (final Throwable t) {

			// Catch server errors
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

			try {
				config.errorHandler().onError(request, response, t);
			} catch (final Throwable t2) {
				response.write(t.getClass()
						+ " was thrown while processing this request.  Additionally, "
						+ t2.getClass()
						+ " was thrown while handling this exception.");
			}

			config.logger().error(request, response, t);

		} finally {

			// If handler did not request async response, finish request
			if (!response.isFinished() && !response.isSuspended()) {
				response.finish();
			}

		}

	}

	private void sendNotFound(final ChannelHandlerContext ctx,
			final FullHttpRequest msg) throws Exception {

		// Create request/response
		final PooledServerRequest request = messagePool.getRequest();
		request.init(ctx.channel(), msg, msg.getUri());

		final PooledServerResponse response = messagePool.getResponse();
		response.init(ctx, this, null, request, config.logger());
		response.setStatus(HttpResponseStatus.NOT_FOUND);

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_RESPONSE).set(response);

		try {

			// Process request
			config.errorHandler().onError(request, response, null);

		} catch (final Throwable e) {

			response.write("The requested URL was not found.  Additionally, "
					+ e.getClass() + " was thrown while handling this error.");

		} finally {

			// If handler did not request async response, finish request
			if (!response.isFinished() && !response.isSuspended()) {
				response.finish();
			}

		}

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {

		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null) {

			try {

				if (!response.isFinished()) {

					response.close();

					final RequestHandler handler = response.handler();

					if (handler != null) {
						handler.onAbort(response.request(), response);
					}

				}

			} finally {

				freeHandlers(ctx);

			}

		}

	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx,
			final Throwable exception) throws Exception {

		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null) {

			try {

				try {

					if (!response.isFinished()) {

						response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

						config.errorHandler().onError(response.request(),
								response, exception);

						response.close();

						final RequestHandler handler = response.handler();

						if (handler != null) {
							handler.onException(response.request(), response,
									exception);
						}

					}

				} finally {

					config.logger().error(response.request(), response,
							exception);

				}

			} finally {

				freeHandlers(ctx);

			}

		}

	}

	/**
	 * Free any request/response handlers related to the current channel handler
	 * context.
	 */
	public void freeHandlers(final ChannelHandlerContext ctx) {

		final PooledServerResponse response =
				ctx.attr(ATTR_RESPONSE).getAndRemove();

		if (response != null) {

			try {

				final RequestHandler handler = response.handler();

				if (handler != null) {
					handler.onComplete(response.request(), response);
				}

			} finally {

				messagePool.makeAvailable(response.request());

				response.close();

				messagePool.makeAvailable(response);

			}

		}

	}

}
