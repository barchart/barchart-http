package com.barchart.http.request;

import java.io.IOException;

/**
 * Inbound request handler.
 */
public interface RequestHandler {

	/**
	 * Called when a new request is received from the client.
	 */
	public void onRequest(ServerRequest request, ServerResponse response)
			throws IOException;

	/**
	 * Called when the client disconnects before the response is completed.
	 */
	public void onAbort(ServerRequest request, ServerResponse response);

	/**
	 * Called when the current request is completed.
	 */
	public void onComplete(ServerRequest request, ServerResponse response);

}
