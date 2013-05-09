/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextImpl implements ServletContext {

	private static final Logger log = LoggerFactory
			.getLogger(ServletContextImpl.class);

	private final Map<String, Object> attributes =
			new ConcurrentHashMap<String, Object>();

	private final MimetypesFileTypeMap mimeTypes;

	/**
	 * The configuration of the servlet.
	 */
	private final LocalServletConfig _config;

	/**
	 * The root URL for the servlet.
	 */
	private final String _rootURL;

	ServletContextImpl(LocalServletConfig config) {
		mimeTypes = new MimetypesFileTypeMap();

		_config = config;
		_rootURL = "jar:" + config.getWarFile().toURI().toString() + "!";
	}

	/*
	 * Servlet 2.5
	 */

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public ServletContext getContext(final String uripath) {
		return this;
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public String getMimeType(final String file) {
		return mimeTypes.getContentType(file);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getResourcePaths(final String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getResource(String str) throws MalformedURLException {
		if (!str.startsWith("/")) {
			str = "/" + str;
		}
		try {
			return new URL(_rootURL + str);
		} catch (MalformedURLException muex) {
			log.warn(_rootURL + str);
			return null;
		}
	}

	@Override
	public InputStream getResourceAsStream(final String str) {
		try {
			JarFile warFile = new JarFile(_config.getWarFile());
			JarEntry entry = warFile.getJarEntry(str);
			if (entry == null) {
				log.warn(str, "No entry.");
				return null;
			} else {
				return warFile.getInputStream(entry);
			}
		} catch (IOException ioe) {
			log.warn(str, ioe.getMessage());
			return null;
		}
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(final String name) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Servlet getServlet(final String name) throws ServletException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServlets() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getServletNames() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public void log(final String msg) {
		log.info(msg);
	}

	@Override
	public void log(final Exception exception, final String msg) {
		log.info(msg, exception);
	}

	@Override
	public void log(final String message, final Throwable throwable) {
		log.info(message, throwable);
	}

	@Override
	public String getRealPath(final String path) {
		return null;
	}

	@Override
	public String getServerInfo() {
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		String os = osName + " " + osVersion + "/" + osArch;

		return "Barchart HTTP v1.0 (" + os + ')';
	}

	@Override
	public String getInitParameter(final String name) {
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {

		return new Enumeration() {

			@Override
			public boolean hasMoreElements() {
				return false;
			}

			@Override
			public Object nextElement() {
				return null;
			}
		};

	}

	@Override
	public Object getAttribute(final String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {

		final Iterator<String> iter = attributes.keySet().iterator();

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
	public void setAttribute(final String name, final Object object) {
		attributes.put(name, object);
	}

	@Override
	public void removeAttribute(final String name) {
		attributes.remove(name);
	}

	@Override
	public String getServletContextName() {
		return null;
	}

	/*
	 * Servlet 3.0
	 */

	@Override
	public boolean setInitParameter(final String name, final String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Dynamic addServlet(final String servletName, final String className)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(final String servletName, final Servlet servlet)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addServlet(final String servletName,
			final Class<? extends Servlet> clazz)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(final Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(final String servletName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			final String filterName, final String className)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			final String filterName, final Filter filter)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(
			final String filterName, final Class<? extends Filter> filterClass)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(final Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FilterRegistration getFilterRegistration(final String filterName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(final Class<? extends EventListener> listenerClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(final String className) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> void addListener(final T t) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> T createListener(final Class<T> clazz)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareRoles(final String... roleNames) {
		// TODO Auto-generated method stub

	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionTrackingModes(
			final Set<SessionTrackingMode> sessionTrackingModes) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getEffectiveMajorVersion() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEffectiveMinorVersion() throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

}
