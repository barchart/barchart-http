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

	public String getQueryString();

	public String getHandlerUri();

	public String getPathInfo();

	public String getScheme();

	public String getRemoteUser();

	public String getServerHost();

	public InetSocketAddress getServerAddress();

	public InetSocketAddress getRemoteAddress();

	public boolean isSecure();

	/* Request parameters */

	public Map<String, List<String>> getParameters();

	public String getParameter(String name);

	public List<String> getParameterList(String name);

	public Map<String, Cookie> getCookies();

	public Cookie getCookie(String name);

	/* Request content */

	public Charset getCharacterEncoding();

	public String getContentType();

	public long getContentLength();

	public InputStream getInputStream();

	public BufferedReader getReader();

	/* Arbitrary attributes */

	public <T> RequestAttribute<T> attr(RequestAttributeKey<T> key);

}
