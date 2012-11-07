package com.barchart.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpTransferEncoding;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.barchart.http.api.RequestAttribute;
import com.barchart.http.api.RequestAttributeKey;
import com.barchart.http.api.ServerRequest;

class ServerRequestImpl implements ServerRequest {

	private HttpRequest nettyRequest;

	private String pathInfo;
	private String queryString;

	private Map<String, List<String>> queryStringDecoded = null;
	private Map<String, Cookie> cookies;

	private Map<RequestAttributeKey<?>, RequestAttribute<?>> attributes;

	private String remoteUser = null;

	ServerRequestImpl() {
	}

	void init(final HttpRequest nettyRequest_, final String relativeUri_) {

		nettyRequest = nettyRequest_;

		final int q = relativeUri_.indexOf('?');

		if (q == -1) {
			pathInfo = relativeUri_;
			queryString = null;
		} else {
			pathInfo = relativeUri_.substring(0, q);
			queryString = relativeUri_.substring(q + 1);
		}

		// TODO check user authentication

	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getContentType() {
		return nettyRequest.getHeader("Content-Type");
	}

	public Charset getCharacterEncoding() {

		final String contentType = getContentType();
		final int pos = contentType.indexOf(";");

		if (pos == -1) {
			return CharsetUtil.ISO_8859_1;
		}

		return Charset.forName(contentType.substring(pos + 1).trim());

	}

	@Override
	public long getContentLength() {
		return HttpHeaders.getContentLength(nettyRequest, 0);
	}

	@Override
	public InputStream getInputStream() {
		return new ByteBufInputStream(nettyRequest.getContent());
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(getInputStream(),
				getCharacterEncoding()));
	}

	@Override
	public Map<String, List<String>> getParameters() {

		if (queryStringDecoded == null && queryString != null) {
			queryStringDecoded =
					new QueryStringDecoder(queryString, false).getParameters();
		}

		return queryStringDecoded;

	}

	@Override
	public String getParameter(final String name) {

		final Map<String, List<String>> params = getParameters();

		if (params != null) {

			final List<String> values = params.get(name);

			if (values != null && values.size() > 0) {
				return values.get(0);
			}

		}

		return null;

	}

	@Override
	public List<String> getParameterList(final String name) {

		final Map<String, List<String>> params = getParameters();

		if (params != null) {
			return params.get(name);
		}

		return null;
	}

	@Override
	public Map<String, Cookie> getCookies() {

		if (cookies == null) {

			cookies = new HashMap<String, Cookie>();

			final Set<Cookie> cookieSet =
					CookieDecoder.decode(nettyRequest.getHeader("Cookie"));

			for (final Cookie cookie : cookieSet) {
				cookies.put(cookie.getName(), cookie);
			}

		}

		return cookies;

	}

	@Override
	public Cookie getCookie(final String name) {

		final Map<String, Cookie> cookies = getCookies();

		if (cookies != null) {
			return cookies.get(name);
		}

		return null;
	}

	public void setRemoteUser(final String user) {
		remoteUser = user;
	}

	@Override
	public String getRemoteUser() {
		return remoteUser;
	}

	/*
	 * Delegate to HttpRequest
	 */

	@Override
	public HttpMethod getMethod() {
		return nettyRequest.getMethod();
	}

	@Override
	public void setMethod(final HttpMethod method) {
		nettyRequest.setMethod(method);
	}

	@Override
	public String getUri() {
		return nettyRequest.getUri();
	}

	@Override
	public void setUri(final String uri) {
		nettyRequest.setUri(uri);
	}

	@Override
	public String getHeader(final String name) {
		return nettyRequest.getHeader(name);
	}

	@Override
	public List<String> getHeaders(final String name) {
		return nettyRequest.getHeaders(name);
	}

	@Override
	public List<Entry<String, String>> getHeaders() {
		return nettyRequest.getHeaders();
	}

	@Override
	public boolean containsHeader(final String name) {
		return nettyRequest.containsHeader(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return nettyRequest.getHeaderNames();
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return nettyRequest.getProtocolVersion();
	}

	@Override
	public void setProtocolVersion(final HttpVersion version) {
		nettyRequest.setProtocolVersion(version);
	}

	@Override
	public ByteBuf getContent() {
		return nettyRequest.getContent();
	}

	@Override
	public void setContent(final ByteBuf content) {
		nettyRequest.setContent(content);
	}

	@Override
	public void addHeader(final String name, final Object value) {
		nettyRequest.addHeader(name, value);
	}

	@Override
	public void setHeader(final String name, final Object value) {
		nettyRequest.setHeader(name, value);
	}

	@Override
	public void setHeader(final String name, final Iterable<?> values) {
		nettyRequest.setHeader(name, values);
	}

	@Override
	public void removeHeader(final String name) {
		nettyRequest.removeHeader(name);
	}

	@Override
	public void clearHeaders() {
		nettyRequest.clearHeaders();
	}

	@Override
	public HttpTransferEncoding getTransferEncoding() {
		return nettyRequest.getTransferEncoding();
	}

	@Override
	public void setTransferEncoding(final HttpTransferEncoding te) {
		nettyRequest.setTransferEncoding(te);
	}

	@Override
	public DecoderResult getDecoderResult() {
		return nettyRequest.getDecoderResult();
	}

	@Override
	public void setDecoderResult(final DecoderResult result) {
		nettyRequest.setDecoderResult(result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T> RequestAttribute<T> attr(
			final RequestAttributeKey<T> key) {

		if (attributes == null) {
			attributes =
					new HashMap<RequestAttributeKey<?>, RequestAttribute<?>>(2);
		}

		RequestAttribute<T> attr = (RequestAttribute<T>) attributes.get(key);
		if (attr == null) {
			attr = new RequestAttribute<T>();
			attributes.put(key, attr);
		}

		return attr;

	}

}
