package com.barchart.http.api;

import io.netty.handler.codec.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Encapsulates a response to an inbound ServerRequest.
 */
public interface ServerResponse extends HttpResponse {

	public void setCookie(String name, String value);

	public void setCharacterEncoding(String charSet);

	public void setContentLength(int length);

	public void setContentType(String mimeType);

	public void sendRedirect(String location);

	public OutputStream getOutputStream();

	public Writer getWriter();

	public void write(byte[] data) throws IOException;

	public void write(byte[] data, int offset, int length) throws IOException;

	public void suspend();

	public boolean isSuspended();

	public void finish();

	public boolean isFinished();

}
