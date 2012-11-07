package com.barchart.http.request;

/**
 * Convenience tuple for returning the results of a RequestHandler / path prefix
 * lookup.
 */
public class RequestHandlerMapping {

	private final String path;
	private final RequestHandler handler;

	public RequestHandlerMapping(final String path_, final RequestHandler handler_) {
		handler = handler_;
		path = path_;
	}

	public String path() {
		return path;
	}

	public RequestHandler handler() {
		return handler;
	}

}
