package com.barchart.http.server;

import java.io.IOException;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * Error handler for failed requests.
 */
public interface ErrorHandler {

	/**
	 * Called when an error occurs during a request.
	 */
	public void onError(final ServerRequest request,
			final ServerResponse response, Exception cause) throws IOException;

}
