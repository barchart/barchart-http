package com.barchart.http.api;

/**
 * Key for storing and retrieving request attributes.
 */
public class RequestAttributeKey<T> {

	private final String name;

	public RequestAttributeKey(final String name_) {
		name = name_;
	}

}
