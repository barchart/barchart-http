/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.request;

public interface RequestURLMapper {

	/**
	 * Find the correct request handler for the given request URI.
	 */
	public RequestHandlerMapping getHandlerFor(String uri);

}
