package com.barchart.http.request;

import java.io.IOException;

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
