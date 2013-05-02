/*
 * $Id: XINSServletRequest.java,v 1.43 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * This class is an implementation of the HTTPServletRequest that can be called
 * localy.
 * 
 * @version $Revision: 1.43 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * 
 * @author Maurycy - modified for Netty 4.0.0 and servlet API 3.0
 * 
 */
public class HttpServletRequestWrapper implements HttpServletRequest {

	/**
	 * The localhost name.
	 */
	private static String LOCALHOST_NAME;

	/**
	 * The localhost address.
	 */
	private static String LOCALHOST_ADDRESS;

	/**
	 * The HTTP sessions of the servlet container.
	 */
	private static Map SESSIONS = new HashMap();

	/**
	 * The HTTP request method.
	 */
	private final String _method;

	/**
	 * The requested URL including the optional parameters.
	 */
	private final String _url;

	/**
	 * The parameters retrieved from the URL.
	 */
	private final HashMap _parameters = new HashMap();

	/**
	 * The date when the request was created.
	 */
	private final long _date = System.currentTimeMillis();

	/**
	 * The attributes of the request.
	 */
	private final Hashtable _attributes = new Hashtable();

	/**
	 * The HTTP headers of the request.
	 */
	private final Hashtable _headers = new Hashtable();

	/**
	 * The URL path.
	 */
	private String _pathInfo;

	/**
	 * The URL query string.
	 */
	private String _queryString;

	/**
	 * The content type of the query.
	 */
	private String _contentType;

	/**
	 * The content of the HTTP POST.
	 */
	private final String _postData;

	/**
	 * The cookies of the request.
	 */
	private Cookie[] _cookies;

	/**
	 * Flags indicating that the input stream has been used.
	 */
	private boolean _inputStreamUsed = false;

	/**
	 * Flags indicating that the reader has been used.
	 */
	private boolean _readerUsed = false;

	/**
	 * Flags indicating that the reader has been used.
	 */
	private String _characterEncoding;

	static {
		try {
			LOCALHOST_ADDRESS = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException exception) {
			LOCALHOST_ADDRESS = "127.0.0.1";
		}

		try {
			LOCALHOST_NAME = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException exception) {
			LOCALHOST_NAME = "localhost";
		}
	}

	/**
	 * Creates a new Servlet request.
	 * 
	 * @param url
	 *            the request URL or the list of the parameters (name=value)
	 *            separated with comma's. Cannot be <code>null</code>.
	 */
	public HttpServletRequestWrapper(String url) {
		this("GET", url, null, null);
	}

	/**
	 * Creates a new servlet request with the specified method.
	 * 
	 * @param method
	 *            the request method, cannot be <code>null</code>.
	 * 
	 * @param url
	 *            the request URL or the list of the parameters (name=value)
	 *            separated with ampersands, cannot be <code>null</code>.
	 * 
	 * @param data
	 *            the content of the request, can be <code>null</code>.
	 * 
	 * @param headers
	 *            the HTTP headers of the request. The key and the value of the
	 *            Map is a String. The keys should all be in upper case. Can be
	 *            <code>null</code>.
	 * 
	 * @since XINS 1.5.0
	 */
	public HttpServletRequestWrapper(String method, String url, String data,
			Map headers) {
		_method = method;
		_url = url;
		_postData = data;
		if (headers == null) {
			_contentType = "application/x-www-form-urlencoded";
		} else {
			_headers.putAll(headers);
			_contentType = (String) headers.get("CONTENT-TYPE");
		}
		parseURL(url);
	}

	/**
	 * Parses the url to extract the parameters.
	 * 
	 * @param url
	 *            the request URL or the list of the parameters (name=value)
	 *            separated with ampersands.
	 */
	private void parseURL(String url) {

		// Parse the URL
		int doubleSlashPos = url.lastIndexOf("://");
		int startPathPos = url.indexOf("/", doubleSlashPos + 1) + 1;

		int questionMarkPos = url.lastIndexOf('?');
		if (questionMarkPos == url.length() - 1) {
			_queryString = "";
			if (startPathPos < questionMarkPos) {
				_pathInfo = url.substring(startPathPos, questionMarkPos);
			}
		} else if (questionMarkPos != -1) {
			_queryString = url.substring(questionMarkPos + 1);
			if (startPathPos < questionMarkPos) {
				_pathInfo = url.substring(startPathPos, questionMarkPos);
			}
		} else {
			_queryString = null;
			if (startPathPos < url.length()) {
				_pathInfo = url.substring(startPathPos);
			}
			return;
		}

		StringTokenizer paramsParser = new StringTokenizer(_queryString, "&");
		while (paramsParser.hasMoreTokens()) {
			String parameter = paramsParser.nextToken();
			int equalPos = parameter.indexOf('=');
			if (equalPos != -1) {
				try {
					String paramName =
							URLDecoder.decode(parameter.substring(0, equalPos));
					String paramValue = "";
					if (equalPos != parameter.length() - 1) {
						paramValue =
								URLDecoder.decode(parameter
										.substring(equalPos + 1));
					}
					Object currValue = _parameters.get(paramName);
					if (currValue == null) {
						_parameters.put(paramName, paramValue);
					} else if (currValue instanceof String) {
						ArrayList<Object> values = new ArrayList<Object>();
						values.add(currValue);
						values.add(paramValue);
						_parameters.put(paramName, values);
					} else {
						ArrayList values = (ArrayList) currValue;
						values.add(paramValue);
					}
				} catch (Exception fe) {
					// Ignore parameter
				}
			}
		}
	}

	@Override
	public void setCharacterEncoding(String str) {
		_characterEncoding = str;
	}

	@Override
	public String[] getParameterValues(String str) {
		Object values = _parameters.get(str);
		if (values == null) {
			return null;
		} else if (values instanceof String) {
			return new String[] { (String) values };
		} else {
			ArrayList list = (ArrayList) values;
			return (String[]) list.toArray(new String[list.size()]);
		}
	}

	@Override
	public String getParameter(String str) {
		String[] values = getParameterValues(str);
		return (values == null) ? null : values[0];
	}

	@Override
	public int getIntHeader(String str) {
		String value = getHeader(str);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException exception) {
				return -1;
			}
		} else {
			return -1;
		}
	}

	@Override
	public Object getAttribute(String str) {
		return _attributes.get(str);
	}

	@Override
	public long getDateHeader(String str) {
		return _date;
	}

	@Override
	public String getHeader(String str) {
		return (String) _headers.get(str.toUpperCase());
	}

	@Override
	public Enumeration getHeaders(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String str) {
		return false;
	}

	@Override
	public void removeAttribute(String str) {
		_attributes.remove(str);
	}

	@Override
	public void setAttribute(String str, Object obj) {
		_attributes.put(str, obj);
	}

	@Override
	public String getQueryString() {
		return _queryString;
	}

	@Override
	public String getProtocol() {
		return "file://";
	}

	@Override
	public String getPathTranslated() {
		// We consider that the first dir is the servlet name
		if (_pathInfo == null) {
			return null;
		}
		int firstSlashPos = _pathInfo.indexOf("/");
		if (firstSlashPos == -1 || firstSlashPos == _pathInfo.length() - 1) {
			return null;
		}
		return _pathInfo.substring(firstSlashPos + 1);
	}

	@Override
	public String getPathInfo() {
		return _pathInfo;
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(_parameters.keySet());
	}

	@Override
	public Map getParameterMap() {
		return _parameters;
	}

	@Override
	public String getMethod() {
		return _method;
	}

	@Override
	public Enumeration getLocales() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getAttributeNames() {
		return _attributes.keys();
	}

	@Override
	public String getAuthType() {
		return "";
	}

	@Override
	public String getCharacterEncoding() {
		if (_characterEncoding != null) {
			return _characterEncoding;
		} else if (_contentType == null) {
			return null;
		} else {
			int charsetPos = _contentType.indexOf("charset=");
			if (charsetPos == -1) {
				return "UTF-8";
			} else {
				return _contentType.substring(charsetPos + 8);
			}
		}
	}

	@Override
	public int getContentLength() {
		return getIntHeader("Content-Length");
	}

	@Override
	public String getContentType() {
		return _contentType;
	}

	@Override
	public String getContextPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cookie[] getCookies() {
		if (_cookies == null && _headers.get("COOKIE") != null) {
			String cookies = (String) _headers.get("COOKIE");
			StringTokenizer stCookies = new StringTokenizer(cookies, ";");
			_cookies = new Cookie[stCookies.countTokens()];
			int counter = 0;
			while (stCookies.hasMoreTokens()) {
				String nextCookie = stCookies.nextToken().trim();
				int equalsPos = nextCookie.indexOf('=');
				String cookieName = nextCookie.substring(0, equalsPos);
				String cookieValue = nextCookie.substring(equalsPos + 1);
				_cookies[counter++] = new Cookie(cookieName, cookieValue);
			}
		} else if (_cookies == null) {
			_cookies = new Cookie[0];
		}
		return _cookies;
	}

	@Override
	public Enumeration getHeaderNames() {
		return _headers.keys();
	}

	@Override
	public ServletInputStream getInputStream() {
		if (_readerUsed) {
			throw new IllegalStateException(
					"The method getReader() has already been called on this request.");
		}
		_inputStreamUsed = true;
		return new InputStream(_postData);
	}

	@Override
	public BufferedReader getReader() {
		if (_inputStreamUsed) {
			throw new IllegalStateException(
					"The method getInputStream() has already been called on this request.");
		}
		_readerUsed = true;
		return new BufferedReader(new StringReader(_postData));
	}

	@Override
	public String getRemoteAddr() {
		return LOCALHOST_ADDRESS;
	}

	@Override
	public String getRemoteHost() {
		return LOCALHOST_NAME;
	}

	@Override
	public String getRemoteUser() {
		return "";
	}

	@Override
	public String getRequestURI() {
		if (_url.indexOf('?') == -1) {
			return _url;
		} else {
			return _url.substring(0, _url.indexOf('?'));
		}
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer(_url);
	}

	@Override
	public String getRequestedSessionId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getScheme() {
		int separator = _url.indexOf("://");
		if (separator != -1) {
			return _url.substring(0, separator + 3);
		}
		return "file://";
	}

	@Override
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception ioe) {
			return "localhost";
		}
	}

	@Override
	public int getServerPort() {
		return 8080;
	}

	@Override
	public String getServletPath() {
		return "";
	}

	@Override
	public HttpSession getSession() {
		return (HttpSession) SESSIONS.get(getRemoteAddr() + getRemoteUser());
	}

	@Override
	public HttpSession getSession(boolean create) {
		String sessionKey = getRemoteAddr() + getRemoteUser();
		HttpSession session = (HttpSession) SESSIONS.get(sessionKey);
		if (session == null) {
			session = new HttpSessionWrapper();
			SESSIONS.put(sessionKey, session);
		}
		return session;
	}

	@Override
	public Principal getUserPrincipal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	/**
	 * Implementation of a <code>ServletInputStream</code> for this request.
	 * 
	 * <p>
	 * This implementation is <strong>not thread-safe</strong>.
	 * 
	 * @version $Revision: 1.43 $ $Date: 2010/09/29 17:21:48 $
	 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
	 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
	 */
	private static class InputStream extends ServletInputStream {
		/**
		 * Constructs a new <code>InputStream</code> instance for the specified
		 * data.
		 * 
		 * @param data
		 *            the data, as a string, can be <code>null</code>.
		 */
		private InputStream(String data) {
			String encoding = "ISO-8859-1";
			try {
				byte[] dataAsByte = data.getBytes(encoding);
				_stream = new ByteArrayInputStream(dataAsByte);
			} catch (UnsupportedEncodingException exception) {
				throw new RuntimeException(
						"Failed to convert characters to bytes using encoding \""
								+ encoding + "\".");
			}
		}

		/**
		 * The data. Is <code>null</code> if there is no data.
		 */
		private final ByteArrayInputStream _stream;

		@Override
		public int read() throws IOException {
			return _stream.read();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return _stream.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return _stream.read(b, off, len);
		}

		@Override
		public boolean markSupported() {
			return _stream.markSupported();
		}

		@Override
		public void mark(int readlimit) {
			_stream.mark(readlimit);
		}

		@Override
		public long skip(long n) throws IOException {
			return _stream.skip(n);
		}

		@Override
		public void reset() throws IOException {
			_stream.reset();
		}

		@Override
		public void close() throws IOException {
			_stream.close();
		}
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Part getPart(String arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}
}
