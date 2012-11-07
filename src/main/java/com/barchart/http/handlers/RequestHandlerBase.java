package com.barchart.http.handlers;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

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
