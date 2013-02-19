package com.barchart.http.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

public class ServletWrapper implements RequestHandler {

	private static final Logger log = LoggerFactory
			.getLogger(ServletWrapper.class);

	private final Servlet servlet;
	private final ServletContextImpl context;

	ServletWrapper(final Servlet servlet_, final ServletContextImpl context_) {
		servlet = servlet_;
		context = context_;
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
