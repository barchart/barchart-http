package com.barchart.http.server;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * HTTP request logging API.
 */
public interface RequestLogger {

	/**
	 * Log a completed request.
	 */
	public void access(ServerRequest request, ServerResponse response,
			long duration);

	/**
	 * Log a failed request.
	 */
	public void error(ServerRequest request, ServerResponse response,
			Throwable exception);

}
