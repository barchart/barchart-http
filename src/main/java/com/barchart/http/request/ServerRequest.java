/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.request;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Information about an inbound request.
 */
public interface ServerRequest extends HttpRequest {

	/* Request metadata */

	/**
	 * The request query string.
	 */
	public String getQueryString();

	/**
	 * The base URI for this request handler.
	 */
	public String getHandlerUri();

	/**
	 * The extra path info after the base URI (see getHandlerUri()).
	 */
	public String getPathInfo();

	/**
	 * The protocol scheme (http, https, etc)
	 */
	public String getScheme();

	/**
	 * The authenticated user for this request. Authentication is done via an
	 * AuthorizationHandler.
	 */
	public String getRemoteUser();

	/**
	 * The server hostname of this request.
	 */
	public String getServerHost();

	/**
	 * The local IP address of the server.
	 */
	public InetSocketAddress getServerAddress();

	/**
	 * The remote client's IP address.
	 */
	public InetSocketAddress getRemoteAddress();

	/**
	 * True if connection is encrypted (SSL).
	 */
	public boolean isSecure();

	/* Request parameters */

	/**
	 * A map of parsed query string parameters.
	 */
	public Map<String, List<String>> getParameters();

	/**
	 * Get a single query parameter by name.
	 */
	public String getParameter(String name);

	/**
	 * Get a multi-value query parameter by name.
	 */
	public List<String> getParameterList(String name);

	/**
	 * Get all active cookies for this request.
	 */
	public Map<String, Cookie> getCookies();

	/**
	 * Get an active cookie by name.
	 */
	public Cookie getCookie(String name);

	/* Request content */

	/**
	 * The character encoding for this request.
	 */
	public Charset getCharacterEncoding();

	/**
	 * The request content MIME type.
	 * 
	 * @return
	 */
	public String getContentType();

	/**
	 * The length of the request data in bytes.
	 */
	public long getContentLength();

	/**
	 * Get a raw input stream for reading the request body.
	 */
	public InputStream getInputStream();

	/**
	 * Get a buffered reader for reading the request body, using default
	 * character encoding.
	 */
	public BufferedReader getReader();

	/* Request attributes */

	/**
	 * Get a request-specific attribute. Useful for sharing data between
	 * handlers and filters during a single request.
	 */
	public <T> RequestAttribute<T> attr(RequestAttributeKey<T> key);

}
