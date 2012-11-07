package com.barchart.http.api;

public interface RequestURLMapper {

	/**
	 * Find the correct request handler for the given request URI.
	 */
	public HandlerURIMapping getHandlerFor(String uri);

}
