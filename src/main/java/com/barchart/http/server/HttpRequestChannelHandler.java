package com.barchart.http.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;

import java.io.IOException;

import com.barchart.http.api.ErrorHandler;
import com.barchart.http.api.HandlerURIMapping;
import com.barchart.http.api.RequestHandler;
import com.barchart.http.api.RequestURLMapper;

/**
 * Netty channel handler for routing inbound requests to the proper
 * RequestHandler.
 */
@Sharable
public class HttpRequestChannelHandler extends
		ChannelInboundMessageHandlerAdapter<HttpRequest> {

	private static final AttributeKey<ServerResponseImpl> ATTR_RESPONSE =
			new AttributeKey<ServerResponseImpl>("response");

	private final RequestURLMapper mapper;
	private final ErrorHandler errorHandler;

	public HttpRequestChannelHandler(final RequestURLMapper mapper_) {
		this(mapper_, new DefaultErrorHandler());
	}

	public HttpRequestChannelHandler(final RequestURLMapper mapper_,
			final ErrorHandler errorHandler_) {
		mapper = mapper_;
		errorHandler = errorHandler_;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final HttpRequest msg) throws Exception {

		// Create request handler
		final HandlerURIMapping mapping = mapper.getHandlerFor(msg.getUri());

		if (mapping == null) {
			handleError(ctx, msg, HttpResponseStatus.NOT_FOUND, null);
			return;
		}

		final String relativePath =
				msg.getUri().substring(mapping.path().length());

		// Create request/response
		final ServerRequestImpl request = new ServerRequestImpl();
		request.init(msg, relativePath);

		final ServerResponseImpl response = new ServerResponseImpl();
		response.init(ctx, mapping.handler(), request);

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_RESPONSE).set(response);

		try {

			// Process request
			mapping.handler().onRequest(request, response);

		} catch (final Exception e) {

			// Catch server errors
			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

			try {
				errorHandler.onError(request, response, e);
			} catch (final Exception e2) {
				response.write(e.getClass()
						+ " was thrown while processing this request.  Additionally, "
						+ e2.getClass()
						+ " was thrown while handling this exception.");
			}

		} finally {

			// If handler did not request async response, finish request
			if (!response.isFinished() && !response.isSuspended()) {
				response.finish();
			}

		}

	}

	private void handleError(final ChannelHandlerContext ctx,
			final HttpRequest msg, final HttpResponseStatus status,
			final Exception exception) throws IOException {

		// Create request/response
		final ServerRequestImpl request = new ServerRequestImpl();
		request.init(msg, msg.getUri());

		final ServerResponseImpl response = new ServerResponseImpl();
		response.init(ctx, null, request);
		response.setStatus(status);

		// Store in ChannelHandlerContext for future reference
		ctx.attr(ATTR_RESPONSE).set(response);

		// Process request
		errorHandler.onError(request, response, exception);

		// If handler did not request async response, finish request
		if (!response.isFinished() && !response.isSuspended()) {
			response.finish();
		}

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {

		final ServerResponseImpl response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null && !response.isFinished()) {

			response.close();

			final RequestHandler handler = response.handler();

			if (handler != null) {
				handler.onAbort(response.request(), response);
			}

		}

	}

}
