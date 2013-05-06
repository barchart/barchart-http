/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.auth;

import java.io.IOException;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * Simplified version of Request Handler is used for authorizations
 * 
 */
public interface AuthorizationHandler {

	/**
	 * Get the authorization method this handler can support.
	 */
	public String getMethod();

	/**
	 * Called when a new request is received from the client, response contains
	 * the validation or not i.e 401 code
	 */
	public void onRequest(ServerRequest request, ServerResponse response)
			throws IOException;
}
