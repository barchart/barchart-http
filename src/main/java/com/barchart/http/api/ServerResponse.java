package com.barchart.http.api;

import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Encapsulates a response to an inbound ServerRequest.
 */
public interface ServerResponse extends HttpResponse {

	/**
	 * Send a cookie to the client.
	 */
	public void setCookie(String name, String value);

	/**
	 * Set the character encoding for this response (default is UTF-8).
	 */
	public void setCharacterEncoding(String charSet);

	/**
	 * Set the content-length for this response. This is set automatically by
	 * default if chunked transfer encoding is not active.
	 */
	public void setContentLength(int length);

	/**
	 * Set the response content MIME type.
	 */
	public void setContentType(String mimeType);

	/**
	 * Send a URL redirect to the client.
	 */
	public void sendRedirect(String location);

	/**
	 * Get the raw output stream for writing to the client. Note that unless
	 * chunked transfer encoding is turned on, all output will still be
	 * buffered.
	 */
	public OutputStream getOutputStream();

	/**
	 * Get a writer that writes data directly to the client. Note that unless
	 * chunked transfer encoding is turned on, all output will still be
	 * buffered.
	 */
	public Writer getWriter();

	/**
	 * Write a string to the client.
	 */
	public void write(String data) throws IOException;

	/**
	 * Write a byte stream to the client.
	 */
	public void write(byte[] data) throws IOException;

	/**
	 * Write a byte stream to the client.
	 */
	public void write(byte[] data, int offset, int length) throws IOException;

	/**
	 * Flush the output buffers. Buffers are flushed automatically, and this
	 * should not usually be necessary.
	 */
	public void flush() throws IOException;

	/**
	 * Mark the request as suspended for future asynchronous responses, which
	 * prevents the framework from closing the response prematurely.
	 */
	public void suspend();

	/**
	 * Check if this response has been suspended.
	 */
	public boolean isSuspended();

	/**
	 * Mark this response as finished, and release any resources associated with
	 * it.
	 */
	public void finish() throws IOException;

	/**
	 * Check if this response has been finished.
	 */
	public boolean isFinished();

}
