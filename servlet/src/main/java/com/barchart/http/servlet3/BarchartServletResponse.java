/*
 * $Id: XINSServletResponse.java,v 1.25 2010/10/25 20:36:52 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is an implementation of the HttpServletResponse that can be
 * invoked locally.
 * 
 * @version $Revision: 1.25 $ $Date: 2010/10/25 20:36:52 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class BarchartServletResponse implements HttpServletResponse {

	/**
	 * The content type of the result. Initially <code>null</code>.
	 */
	private String _contentType;

	/**
	 * The non-negative content length, or a negative number if unset. Initially
	 * a negative number.
	 */
	private int _contentLength = -99;

	/**
	 * The status of the result.
	 */
	private int _status;

	/**
	 * The encoding of the result. Must default to ISO-8859-1, according to the
	 * Java Servlet 2.4 Specification.
	 */
	private String _encoding = "ISO-8859-1";

	/**
	 * The writer where to write the result.
	 */
	private StringWriter _writer;

	/**
	 * The headers.
	 */
	private final Map<String, String> _headers = new HashMap<String, String>();

	/**
	 * Creates a new instance of <code>XINSServletResponse</code>.
	 */
	public BarchartServletResponse() {
	}

	@Override
	public void addDateHeader(String str, long param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDateHeader(String str, long param) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeUrl(String url) {
		return url;
	}

	@Override
	public String encodeURL(String url) {
		return url;
	}

	@Override
	public String encodeRedirectUrl(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeRedirectURL(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsHeader(String str) {
		return _headers.get(str) != null;
	}

	@Override
	public void sendRedirect(String location) {
		setStatus(302);
		setHeader("Location", location);
	}

	/**
	 * Sets the content type.
	 * 
	 * @param type
	 *            the content type, cannot be <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>type == null</code>.
	 */
	@Override
	public void setContentType(String type) throws IllegalArgumentException {

		setHeader("Content-Type", type);

		String search = "charset=";
		int i = type.indexOf(search);
		if (i >= 0) {
			_encoding = type.substring(i + search.length());
		}

		_contentType = type;
	}

	@Override
	public void setStatus(int sc) {
		_status = sc;
	}

	@Override
	public void sendError(int sc) {
		sendError(sc, null);
	}

	@Override
	public void setBufferSize(int param) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the content length.
	 * 
	 * @return the (non-negative) content length if set, or a negative value if
	 *         unset.
	 */
	int getContentLength() {
		return _contentLength;
	}

	@Override
	public void setContentLength(int param) {
		_contentLength = param;
		setIntHeader("Content-Length", param);
	}

	@Override
	public void addCookie(Cookie cookie) {
		String cookieHeader = cookie.getName() + "=" + cookie.getValue();
		if (cookie.getPath() != null) {
			cookieHeader += "; path=" + cookie.getPath();
		}
		if (cookie.getDomain() != null) {
			cookieHeader += "; domain=" + cookie.getDomain();
		}
		setHeader("Set-Cookie", cookieHeader);
	}

	@Override
	public void setLocale(Locale locale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatus(int param, String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIntHeader(String str, int param) {
		setHeader(str, "" + param);
	}

	@Override
	public void addIntHeader(String str, int param) {
		setHeader(str, "" + param);
	}

	@Override
	public void sendError(int sc, String msg) {
		_status = sc;
	}

	@Override
	public void setHeader(String str, String str1) {
		_headers.put(str, str1);
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCharacterEncoding() {
		return _encoding;
	}

	@Override
	public int getBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void flushBuffer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addHeader(String str, String str1) {
		_headers.put(str, str1);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PrintWriter getWriter() {
		_writer = new StringWriter();
		return new PrintWriter(_writer);
	}

	@Override
	public boolean isCommitted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetBuffer() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the returned message from the servlet.
	 * 
	 * @return the returned message or <code>null</code> if no message is
	 *         returned.
	 */
	public String getResult() {
		if (_writer == null) {
			return null;
		}
		return _writer.toString();
	}

	/**
	 * Gets the status of the returned message.
	 * 
	 * @return the HTTP status returned.
	 */
	@Override
	public int getStatus() {
		return _status;
	}

	/**
	 * Gets the type of the returned content.
	 * 
	 * @return the content type, can be <code>null</code>.
	 * 
	 * @see #setContentType(String)
	 */
	@Override
	public String getContentType() {
		return _contentType;
	}

	/**
	 * Gets the headers to return to the client.
	 * 
	 * @return the headers, cannot be <code>null</code>.
	 * 
	 * @since XINS 1.3.0
	 */
	public Map<String, String> getHeaders() {
		return _headers;
	}

	@Override
	public void setCharacterEncoding(String encoding) {
		_encoding = encoding;
	}

	@Override
	public String getHeader(String name) {
		return _headers.get(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return _headers.keySet();
	}

	@Override
	public Collection<String> getHeaders(String arg0) {
		return _headers.values();
	}
}
