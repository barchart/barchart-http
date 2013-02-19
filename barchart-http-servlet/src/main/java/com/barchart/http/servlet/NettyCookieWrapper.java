package com.barchart.http.servlet;

import javax.servlet.http.Cookie;

public class NettyCookieWrapper extends Cookie {

	public NettyCookieWrapper(final io.netty.handler.codec.http.Cookie cookie) {
		super(cookie.getName(), cookie.getValue());
		setComment(cookie.getComment());
		setDomain(cookie.getDomain());
		setMaxAge((int) cookie.getMaxAge());
		setPath(cookie.getPath());
		setSecure(cookie.isSecure());
		setVersion(cookie.getVersion());
	}

}
