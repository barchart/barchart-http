package com.barchart.http.request;

/**
 * Factory for creating RequestHandler instances, which allows more control over
 * request handler lifecycle. Using a RequestHandlerFactory in conjunction with
 * RequestHandler.onComplete() can allow for construction of a cached handler
 * pool while still allowing request-specific state to be stored in the handler.
 */
public interface RequestHandlerFactory {

	/**
	 * Create a new request handler. Subclasses can use this method in
	 * conjunction with onComplete() to implement limited-size request handler
	 * pools to avoid excessive object creation and garbage collection.
	 */
	public RequestHandler newHandler();

}
