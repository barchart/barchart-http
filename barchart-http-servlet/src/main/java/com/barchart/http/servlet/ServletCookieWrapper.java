package com.barchart.http.servlet;

import io.netty.handler.codec.http.DefaultCookie;

public class ServletCookieWrapper extends DefaultCookie {

	public ServletCookieWrapper(final javax.servlet.http.Cookie cookie) {
		super(cookie.getName(), cookie.getValue());
		setComment(cookie.getComment());
		setDomain(cookie.getDomain());
		setMaxAge(cookie.getMaxAge());
		setPath(cookie.getPath());
		setSecure(cookie.getSecure());
		setVersion(cookie.getVersion());
	}

}
