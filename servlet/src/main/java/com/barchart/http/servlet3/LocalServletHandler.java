/*
 * $Id: LocalServletHandler.java,v 1.27 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to invoke a XINS API without using HTTP.
 * 
 * Example: <code>
 * LocalServletHandler handler = LocalServletHandler.getInstance("c:\\test\\myproject.war");
 * String xmlResult = handler.query("http://127.0.0.1:8080/myproject/?_function=MyFunction&gender=f&personLastName=Lee");
 * </code>
 * 
 * @version $Revision: 1.27 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 */
public class LocalServletHandler {

	private static final Logger log = LoggerFactory
			.getLogger(LocalServletHandler.class);

	/**
	 * The Servlet started by this Servlet handler.
	 */
	private HttpServlet _apiServlet;

	/**
	 * Creates a Servlet handler that allow to invoke a Servlet without starting
	 * a HTTP server.
	 * 
	 * @param warFile
	 *            the location of the war file containing the Servlet, cannot be
	 *            <code>null</code>.
	 * 
	 * @throws ServletException
	 *             if the Servlet cannot be created.
	 */
	public LocalServletHandler(File warFile) throws ServletException {
		initServlet(warFile);
	}

	/**
	 * Creates a Servlet handler that allow to invoke a Servlet without starting
	 * a HTTP server.
	 * 
	 * @param servletClassName
	 *            The name of the servlet's class to load, cannot be
	 *            <code>null</code>.
	 * 
	 * @throws ServletException
	 *             if the Servlet cannot be created.
	 */
	public LocalServletHandler(String servletClassName) throws ServletException {
		initServlet(servletClassName);
	}

	/**
	 * Initializes the Servlet.
	 * 
	 * @param warFile
	 *            the location of the war file, cannot be <code>null</code>.
	 * 
	 * @throws ServletException
	 *             if the Servlet cannot be loaded.
	 */
	public void initServlet(File warFile) throws ServletException {
		// create and initiliaze the Servlet
		log.debug(warFile.getPath());
		try {
			LocalServletConfig servletConfig = new LocalServletConfig(warFile);
			_apiServlet =
					(HttpServlet) Class
							.forName(servletConfig.getServletClass())
							.newInstance();
			_apiServlet.init(servletConfig);
		} catch (ServletException exception) {
			log.warn("Exception", exception);
			throw exception;
		} catch (Exception exception) {
			log.warn("Exception", exception);
			throw new ServletException(exception);
		}
	}

	/**
	 * Initializes the Servlet.
	 * 
	 * @param servletClassName
	 *            The name of the servlet's class to load, cannot be
	 *            <code>null</code>.
	 * 
	 * @throws ServletException
	 *             if the Servlet cannot be loaded.
	 */
	public void initServlet(String servletClassName) throws ServletException {
		// create and initiliaze the Servlet
		// Log.log_1503(warFile.getPath());
		try {
			_apiServlet =
					(HttpServlet) Class.forName(servletClassName).newInstance();
			_apiServlet.init();
		} catch (ServletException exception) {
			log.warn("Exception", exception);
			throw exception;
		} catch (Exception exception) {
			log.warn("Exception", exception);
			throw new ServletException(exception);
		}
	}

	/**
	 * Gets the Servlet.
	 * 
	 * @return the created Servlet or <code>null</code> if no Servlet was
	 *         created.
	 */
	public Object getServlet() {
		return _apiServlet;
	}

	/**
	 * Queries the Servlet with the specified URL.
	 * 
	 * @param url
	 *            the url query for the request.
	 * 
	 * @return the servlet response.
	 * 
	 * @throws IOException
	 *             If the query is not handled correctly by the servlet.
	 */
	public BarchartServletResponse query(String url) throws IOException {
		return query("GET", url, null, new HashMap());
	}

	/**
	 * Queries the servlet with the specified method, URL, content and HTTP
	 * headers.
	 * 
	 * @param method
	 *            the request method, cannot be <code>null</code>.
	 * 
	 * @param url
	 *            the url query for the request, if <code>null</code> then the /
	 *            path is used as default with no parameters.
	 * 
	 * @param data
	 *            the data post for the request. <code>null</code> for HTTP GET
	 *            queries.
	 * 
	 * @param headers
	 *            the HTTP headers passed with the query, cannot be
	 *            <code>null</code>. The key and the value of the Map is String.
	 *            The keys are all in uppercase.
	 * 
	 * @return the servlet response.
	 * 
	 * @throws IOException
	 *             If the query is not handled correctly by the servlet.
	 * 
	 * @since XINS 1.5.0
	 */
	public BarchartServletResponse query(String method, String url, String data,
			Map headers) throws IOException {

		log.debug(url);

		BarchartServletRequest request =
				new BarchartServletRequest(method, url, data, headers);
		BarchartServletResponse response = new BarchartServletResponse();
		try {
			_apiServlet.service(request, response);
		} catch (ServletException ex) {
			log.warn("Exception", ex);
			throw new IOException(ex.getMessage());
		}
		log.debug(response.getResult(), response.getStatus());
		return response;
	}

	/**
	 * Disposes the Servlet and closes this Servlet handler.
	 */
	public void close() {
		_apiServlet.destroy();
	}
}
