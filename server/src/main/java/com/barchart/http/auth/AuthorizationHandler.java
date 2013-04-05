/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.auth;

public interface AuthorizationHandler {

	/**
	 * Get the authorization method this handler can support.
	 */
	public String getMethod();

	/**
	 * Attempt to authorize the user with the given authorization data and
	 * return the remote user associated with it. If no authorization succeeds,
	 * this method will return null.
	 */
	public String authorize(String data);

}
