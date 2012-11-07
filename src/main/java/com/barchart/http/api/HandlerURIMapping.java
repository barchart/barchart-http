package com.barchart.http.api;

/**
 * Convenience tuple for returning the results of a RequestHandler / path prefix
 * lookup.
 */
public class HandlerURIMapping {

	private final String path;
	private final RequestHandler handler;

	public HandlerURIMapping(final String path_, final RequestHandler handler_) {
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
