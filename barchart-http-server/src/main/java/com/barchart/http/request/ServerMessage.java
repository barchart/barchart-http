package com.barchart.http.request;

import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpTransferEncoding;
import io.netty.handler.codec.http.HttpVersion;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Information about an inbound request.
 */
public interface ServerMessage extends HttpMessage {

	@Override
	public String getHeader(String name);

	@Override
	public List<String> getHeaders(String name);

	@Override
	public List<Map.Entry<String, String>> getHeaders();

	@Override
	public boolean containsHeader(String name);

	@Override
	public Set<String> getHeaderNames();

	@Override
	public HttpVersion getProtocolVersion();

	@Override
	public void setProtocolVersion(HttpVersion version);

	@Override
	public void addHeader(String name, Object value);

	@Override
	public void setHeader(String name, Object value);

	@Override
	public void setHeader(String name, Iterable<?> values);

	@Override
	public void removeHeader(String name);

	@Override
	public void clearHeaders();

	@Override
	public HttpTransferEncoding getTransferEncoding();

	@Override
	public void setTransferEncoding(HttpTransferEncoding te);

}
