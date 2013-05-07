/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

public class ServletWrapper implements RequestHandler {

	private static final Logger log = LoggerFactory
			.getLogger(ServletWrapper.class);

	private Servlet servlet;
	private ServletContextImpl context;

	ServletWrapper(final Servlet servlet_, final ServletContextImpl context_) {
		servlet = servlet_;
		context = context_;
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
	public ServletWrapper(File warFile, String className) {

		// create and initiliaze the Servlet
		log.debug(warFile.getPath());

		try {
			LocalServletConfig servletConfig = new LocalServletConfig(warFile);

			ClassLoader loader = new WarClassLoader(warFile.getAbsolutePath());

			if (className != null)
				servlet =
						(HttpServlet) loader.loadClass(className).newInstance();
			else
				servlet =
						(HttpServlet) loader.loadClass(
								servletConfig.getServletClass()).newInstance();

			servlet.init(servletConfig);

		} catch (Exception exception) {
			log.warn("Exception", exception);
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
			servlet =
					(HttpServlet) Class.forName(servletClassName).newInstance();
			servlet.init(null);
		} catch (ServletException exception) {
			log.warn("Exception", exception);
			throw exception;
		} catch (Exception exception) {
			log.warn("Exception", exception);
			throw new ServletException(exception);
		}
	}

	@Override
	public void onRequest(final ServerRequest request,
			final ServerResponse response) throws IOException {
		try {
			servlet.service(new HttpServletRequestWrapper(request, context),
					new HttpServletResponseWrapper(response));
		} catch (final ServletException e) {
			log.info("Uncaught servlet exception", e);
		}
	}

	@Override
	public void onException(final ServerRequest request,
			final ServerResponse response, final Throwable exception) {
		log.info("Uncaught exception", exception);
	}

	@Override
	public void onAbort(final ServerRequest request,
			final ServerResponse response) {
		// No-op
	}

	@Override
	public void onComplete(final ServerRequest request,
			final ServerResponse response) {
		// No-op
	}

}
