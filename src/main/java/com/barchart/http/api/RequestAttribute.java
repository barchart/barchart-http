package com.barchart.http.api;

/**
 * An arbitrarily-valued attribute for storing in a ServerRequest object.
 */
public class RequestAttribute<T> {

	private T value = null;

	public void set(final T value_) {
		value = value_;
	}

	public T get() {
		return value;
	}

}
