/*
 * $Id: NettyServletHandler.java,v 1.2 2012/04/27 07:40:28 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * Netty handler to invoke servlets.
 * 
 * This class is partly based on the examples of the Netty project which are
 * released under the Apache License, version 2.0.
 * 
 * @version $Revision: 1.2 $ $Date: 2012/04/27 07:40:28 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * 
 * @author Maurycy - modified for Netty 4.0.0 and servlet API 3.0
 * 
 */
public class NettyServletHandler implements RequestHandler {

	private final LocalServletHandler localServletHandler;

	/**
	 * Creates a Netty handler that allow to invoke a Servlet without starting a
	 * HTTP server.
	 * 
	 * @param warFile
	 *            the location of the war file containing the Servlet, cannot be
	 *            <code>null</code>.
	 * 
	 * @throws ServletException
	 *             if the Servlet cannot be created.
	 */
	public NettyServletHandler(File warFile) throws ServletException {
		localServletHandler = new LocalServletHandler(warFile);
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
	public NettyServletHandler(String servletClassName) throws ServletException {
		localServletHandler = new LocalServletHandler(servletClassName);
	}

	@Override
	public void onRequest(ServerRequest request, ServerResponse response)
			throws IOException {

		String method = request.getMethod().name();
		String url = request.getUri();
		String data = request.toString();

		Map headers = new LinkedHashMap();
		for (Map.Entry<String, String> header : request.headers()) {
			headers.put(header.getKey(), header.getValue());
		}

		// MJS: TBD - add response
		localServletHandler.query(method, url, data, headers);
	}

	@Override
	public void onException(ServerRequest request, ServerResponse response,
			Throwable exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAbort(ServerRequest request, ServerResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onComplete(ServerRequest request, ServerResponse response) {
		// TODO Auto-generated method stub

	}
}
