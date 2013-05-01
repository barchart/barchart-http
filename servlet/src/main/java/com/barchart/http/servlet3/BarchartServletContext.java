/*
 * $Id: XINSServletContext.java,v 1.26 2010/10/02 19:18:29 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an implementation of the ServletContext that can be called
 * locally.
 * 
 * @version $Revision: 1.26 $ $Date: 2010/10/02 19:18:29 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class BarchartServletContext implements ServletContext {

	private static final Logger log = LoggerFactory
			.getLogger(BarchartServletContext.class);

	/**
	 * The configuration of the servlet.
	 */
	private LocalServletConfig _config;

	/**
	 * The root URL for the servlet.
	 */
	private String _rootURL;

	/**
	 * Creates a new <code>XINSServletContext</code> instance.
	 */
	public BarchartServletContext() {
		// empty
	}

	/**
	 * Creates a new <code>XINSServletContext</code> with the specified
	 * configuration.
	 * 
	 * @param config
	 *            the config of the servlet, can be <code>null</code>.
	 */
	BarchartServletContext(LocalServletConfig config) {
		_config = config;
		_rootURL = "jar:" + config.getWarFile().toURI().toString() + "!";
	}

	@Override
	public void removeAttribute(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Servlet getServlet(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set getResourcePaths(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAttribute(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getContext(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getInitParameter(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMimeType(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String str) {

		// The WAR file is not unpacked
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String str) {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL getResource(String str) {
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
	public InputStream getResourceAsStream(String str) {
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
	public void log(Exception exception, String msg) {
		log(msg, exception);
	}

	@Override
	public void log(String msg) {
		log.warn(msg);
	}

	@Override
	public void log(String msg, Throwable throwable) {
		log.warn(throwable.toString(), msg);
	}

	@Override
	public void setAttribute(String str, Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getServlets() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getServletNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServletContextName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerInfo() {
		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		String os = osName + " " + osVersion + "/" + osArch;
		return "XINS Servlet Test Container (" + os + ')';
	}

	@Override
	public Enumeration getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getInitParameterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public int getMinorVersion() {
		return 3;
	}

	@Override
	public Dynamic addFilter(String arg0, String arg1)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addFilter(String arg0, Filter arg1)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1)
			throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(Class<? extends EventListener> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> void addListener(T arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			String arg1) throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Servlet arg1) throws IllegalArgumentException,
			IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1) throws IllegalArgumentException,
			IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends EventListener> T createListener(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareRoles(String... arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
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
	public FilterRegistration getFilterRegistration(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setInitParameter(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
		// TODO Auto-generated method stub

	}
}
