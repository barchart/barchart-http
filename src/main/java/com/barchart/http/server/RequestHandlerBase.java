package com.barchart.http.server;

import com.barchart.http.api.RequestHandler;
import com.barchart.http.api.ServerRequest;
import com.barchart.http.api.ServerResponse;

/**
 * Base request handler that provides default implementations of some less-used
 * methods.
 */
public abstract class RequestHandlerBase implements RequestHandler {

	@Override
	public void onAbort(final ServerRequest request,
			final ServerResponse response) {
	}

	@Override
	public void onComplete(final ServerRequest request,
			final ServerResponse response) {
	}

}
