package com.barchart.http.request;

public interface RequestURLMapper {

	/**
	 * Find the correct request handler for the given request URI.
	 */
	public RequestHandlerMapping getHandlerFor(String uri);

}
