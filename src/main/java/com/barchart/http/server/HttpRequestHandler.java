package com.barchart.http.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

import com.barchart.http.api.RequestHandler;
import com.barchart.http.api.ServerRequest;
import com.barchart.http.api.ServerResponse;

/**
 * Netty channel handler for routing inbound requests to the proper
 * RequestHandler.
 */
@Sharable
public class HttpRequestHandler extends
		ChannelInboundMessageHandlerAdapter<HttpRequest> {

	private static final AttributeKey<RequestHandler> ATTR_REQUEST_HANDLER =
			new AttributeKey<RequestHandler>("request-handler");

	private static final AttributeKey<ServerRequest> ATTR_REQUEST =
			new AttributeKey<ServerRequest>("request");

	private static final AttributeKey<ServerResponse> ATTR_RESPONSE =
			new AttributeKey<ServerResponse>("response");

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final HttpRequest msg) throws Exception {

		// TODO Create request handler
		final String prefix = "";
		final RequestHandler handler = null;

		// Create request/response
		final ServerRequestImpl request = new ServerRequestImpl(msg, prefix);
		final ServerResponseImpl response = new ServerResponseImpl(ctx);

		// TODO Check user authentication
		request.setRemoteUser(null);

		// Store handlers in ChannelHandlerContext for future reference
		ctx.attr(ATTR_REQUEST).set(request);
		ctx.attr(ATTR_RESPONSE).set(response);
		ctx.attr(ATTR_REQUEST_HANDLER).set(handler);

		// 5) Process request
		handler.onRequest(request, response);

	}

	@Override
	public void channelInactive(final ChannelHandlerContext ctx) {

		final ServerResponse response = ctx.attr(ATTR_RESPONSE).get();

		if (response != null && !response.isFinished()) {

			final RequestHandler handler = ctx.attr(ATTR_REQUEST_HANDLER).get();

			if (handler != null) {
				handler.onAbort(ctx.attr(ATTR_REQUEST).get(), response);
			}

		}

	}

}
