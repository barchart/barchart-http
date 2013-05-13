/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import javax.servlet.http.Cookie;

// MJS: This is used during the request
@SuppressWarnings("serial")
public class RequestCookieWrapper extends Cookie {

	public RequestCookieWrapper(final io.netty.handler.codec.http.Cookie cookie) {
		super(cookie.getName(), cookie.getValue());
		setComment(cookie.getComment());
		setDomain(cookie.getDomain());
		setMaxAge((int) cookie.getMaxAge());
		setPath(cookie.getPath());
		setSecure(cookie.isSecure());
		setVersion(cookie.getVersion());
	}
}
