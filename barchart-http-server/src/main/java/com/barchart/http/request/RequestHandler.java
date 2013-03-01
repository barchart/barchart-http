/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
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
	 * Called when the request encounters an exception, either in the pipeline
	 * or as part of async processing.
	 */
	public void onException(ServerRequest request, ServerResponse response,
			Throwable exception);

	/**
	 * Called when the client disconnects before the response is completed.
	 */
	public void onAbort(ServerRequest request, ServerResponse response);

	/**
	 * Called when the current request is completed.
	 */
	public void onComplete(ServerRequest request, ServerResponse response);

}
