package com.barchart.http.servlet;

import io.netty.handler.codec.http.HttpHeaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import com.barchart.http.request.RequestAttributeKey;
import com.barchart.http.request.ServerRequest;

public class HttpServletRequestWrapper implements HttpServletRequest {

	private final Map<String, RequestAttributeKey<Object>> attrKeys =
			new HashMap<String, RequestAttributeKey<Object>>();

	private final ServerRequest request;
	private final ServletContext context;

	HttpServletRequestWrapper(final ServerRequest request_,
			final ServletContextImpl context_) {
		request = request_;
		context = context_;
	}

	/*
	 * Servlet 2.5
	 */

	@Override
	public Object getAttribute(final String name) {
		final RequestAttributeKey<Object> key = attrKeys.get(name);
		if (key != null) {
			return request.attr(key).get();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCharacterEncoding() {
		return request.getCharacterEncoding().name();
	}

	@Override
	public void setCharacterEncoding(final String env)
			throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getContentLength() {
		return (int) request.getContentLength();
	}

	@Override
	public String getContentType() {
		return request.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		final InputStream in = request.getInputStream();

		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return in.read();
			}

		};

	}

	@Override
	public String getParameter(final String name) {
		return request.getParameter(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {

		final Iterator<String> iter =
				request.getParameters().keySet().iterator();

		return new Enumeration() {

			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public Object nextElement() {
				return iter.next();
			}

		};

	}

	@Override
	public String[] getParameterValues(final String name) {
		return request.getParameterList(name).toArray(new String[] {});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {
		return request.getParameters();
	}

	@Override
	public String getProtocol() {
		return request.getProtocolVersion().getText();
	}

	@Override
	public String getScheme() {
		return request.getScheme();
	}

	@Override
	public String getServerName() {
		return request.getServerAddress().getHostString();
	}

	@Override
	public int getServerPort() {
		return request.getServerAddress().getPort();
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	@Override
	public String getRemoteAddr() {
		return request.getRemoteAddress().getAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return request.getRemoteAddress().getHostString();
	}

	@Override
	public void setAttribute(final String name, final Object o) {

		RequestAttributeKey<Object> key = attrKeys.get(name);

		if (key == null) {
			key = new RequestAttributeKey<Object>(name);
			attrKeys.put(name, key);
		}

		request.attr(key).set(o);

	}

	@Override
	public void removeAttribute(final String name) {
		final RequestAttributeKey<Object> key = attrKeys.get(name);
		if (key != null) {
			request.attr(key).set(null);
		}
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getLocales() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {
		return context.getRequestDispatcher(path);
	}

	@Override
	public String getRealPath(final String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemotePort() {
		return request.getRemoteAddress().getPort();
	}

	@Override
	public String getLocalName() {
		return request.getServerAddress().getHostString();
	}

	@Override
	public String getLocalAddr() {
		return request.getServerAddress().getAddress().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return request.getServerAddress().getPort();
	}

	@Override
	public String getAuthType() {
		// TODO
		return null;
	}

	@Override
	public Cookie[] getCookies() {

		final Collection<io.netty.handler.codec.http.Cookie> requestCookies =
				request.getCookies().values();

		final List<Cookie> cookies = new ArrayList<Cookie>();

		for (final io.netty.handler.codec.http.Cookie cookie : requestCookies) {
			cookies.add(new NettyCookieWrapper(cookie));
		}

		return cookies.toArray(new Cookie[] {});

	}

	@Override
	public long getDateHeader(final String name) {
		try {
			return HttpHeaders.getDateHeader(request, name).getTime();
		} catch (final ParseException e) {
			return 0;
		}
	}

	@Override
	public String getHeader(final String name) {
		return request.getHeader(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaders(final String name) {

		final Iterator<String> iter = request.getHeaders(name).iterator();

		return new Enumeration() {

			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public Object nextElement() {
				return iter.next();
			}

		};

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getHeaderNames() {

		final Iterator<String> iter = request.getHeaderNames().iterator();

		return new Enumeration() {

			@Override
			public boolean hasMoreElements() {
				return iter.hasNext();
			}

			@Override
			public Object nextElement() {
				return iter.next();
			}

		};

	}

	@Override
	public int getIntHeader(final String name) {
		return HttpHeaders.getIntHeader(request, name);
	}

	@Override
	public String getMethod() {
		return request.getMethod().getName();
	}

	@Override
	public String getPathInfo() {
		return request.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return request.getPathInfo();
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public String getQueryString() {
		return request.getQueryString();
	}

	@Override
	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	@Override
	public boolean isUserInRole(final String role) {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return request.getUri();
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServletPath() {
		return request.getHandlerUri();
	}

	@Override
	public HttpSession getSession(final boolean create) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
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

	/*
	 * Servlet 3.0
	 */

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
	public AsyncContext startAsync(final ServletRequest request,
			final ServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(final HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Part getPart(final String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(final String username, final String password)
			throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

}
