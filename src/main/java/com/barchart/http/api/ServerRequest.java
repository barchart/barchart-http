package com.barchart.http.api;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Information about an inbound request.
 */
public interface ServerRequest extends HttpRequest {

	/* Request metadata */

	public String getQueryString();

	public String getPathInfo();

	public String getRemoteUser();

	/* Request parameters */

	public Map<String, List<String>> getParameters();

	public String getParameter(String name);

	public List<String> getParameterList(String name);

	public Map<String, Cookie> getCookies();

	public Cookie getCookie(String name);

	/* Request content */

	public String getContentType();

	public long getContentLength();

	public InputStream getInputStream();

	public BufferedReader getReader();

}
