/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.barchart.http.request.ServerResponse;

/**
 * Wrap Netty HTTP response in a Servlet-compatible object.
 */
public class HttpServletResponseWrapper implements HttpServletResponse {

	private final ServerResponse response;

	HttpServletResponseWrapper(final ServerResponse response_) {
		response = response_;
	}

	/*
	 * Servlet 2.5
	 */

	@Override
	public String getCharacterEncoding() {
		return response.getCharacterEncoding().name();
	}

	@Override
	public String getContentType() {
		return response.headers().get(HttpHeaders.Names.CONTENT_TYPE);
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		final OutputStream out = response.getOutputStream();

		return new ServletOutputStream() {

			@Override
			public void write(final int b) throws IOException {
				out.write(b);
			}

		};
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(response.getWriter());
	}

	@Override
	public void setCharacterEncoding(final String charset) {
		response.setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(final int len) {
		response.setContentLength(len);
	}

	@Override
	public void setContentType(final String type) {
		response.setContentType(type);
	}

	@Override
	public void setBufferSize(final int size) {
		// Noop
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		response.flush();
	}

	@Override
	public void resetBuffer() {
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
	}

	Locale locale = Locale.getDefault();

	@Override
	public void setLocale(final Locale loc) {
		locale = loc;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public void addCookie(final Cookie cookie) {
		response.setCookie(new ServletCookieWrapper(cookie));
	}

	@Override
	public boolean containsHeader(final String name) {
		return response.headers().contains(name);
	}

	@Override
	public String encodeURL(final String url) {
		return url;
	}

	@Override
	public String encodeRedirectURL(final String url) {
		return url;
	}

	@Override
	public String encodeUrl(final String url) {
		return url;
	}

	@Override
	public String encodeRedirectUrl(final String url) {
		return url;
	}

	@Override
	public void sendError(final int sc, final String msg) throws IOException {
		response.setStatus(HttpResponseStatus.valueOf(sc));
		response.write(msg.getBytes());
	}

	@Override
	public void sendError(final int sc) throws IOException {
		response.setStatus(HttpResponseStatus.valueOf(sc));
	}

	@Override
	public void sendRedirect(final String location) throws IOException {
		response.sendRedirect(location);
	}

	@Override
	public void setDateHeader(final String name, final long date) {
		response.headers().set(name, date);
	}

	@Override
	public void addDateHeader(final String name, final long date) {
		response.headers().add(name, date);
	}

	@Override
	public void setHeader(final String name, final String value) {
		response.headers().set(name, value);
	}

	@Override
	public void addHeader(final String name, final String value) {
		response.headers().add(name, value);
	}

	@Override
	public void setIntHeader(final String name, final int value) {
		response.headers().set(name, value);
	}

	@Override
	public void addIntHeader(final String name, final int value) {
		response.headers().add(name, value);
	}

	@Override
	public void setStatus(final int sc) {
		response.setStatus(HttpResponseStatus.valueOf(sc));
	}

	@Override
	public void setStatus(final int sc, final String sm) {
		response.setStatus(HttpResponseStatus.valueOf(sc));
	}

	/*
	 * Servlet 3.0
	 */

	@Override
	public String getHeader(final String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(final String headerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

}
