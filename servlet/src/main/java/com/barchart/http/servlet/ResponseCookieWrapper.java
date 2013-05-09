/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import io.netty.handler.codec.http.DefaultCookie;

import javax.servlet.http.Cookie;

// MJS: This is used for the response
public class ResponseCookieWrapper extends DefaultCookie {

	public ResponseCookieWrapper(final Cookie cookie) {
		super(cookie.getName(), cookie.getValue());
		setComment(cookie.getComment());
		setDomain(cookie.getDomain());
		setMaxAge(cookie.getMaxAge());
		setPath(cookie.getPath());
		setSecure(cookie.getSecure());
		setVersion(cookie.getVersion());
	}

}
