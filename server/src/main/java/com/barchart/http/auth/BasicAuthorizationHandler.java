/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.auth;

import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;
import com.barchart.util.values.base64.Base64;

/**
 * Implements Basic HTTP authorization handling.
 * 
 * @author jeremy
 * 
 */
public class BasicAuthorizationHandler implements AuthorizationHandler {

	private final Authenticator authenticator;

	public BasicAuthorizationHandler(final Authenticator authenticator_) {
		authenticator = authenticator_;
	}

	@Override
	public String getMethod() {
		return "Basic";
	}

	// MJS: We implement Basic authentication per
	// http://tools.ietf.org/html/rfc2617

	@Override
	public void authenticate(final ServerRequest request,
			final ServerResponse response) throws IOException {

		final String authHeader = request.headers().get("Authorization");

		if (authHeader == null || authHeader.equals("")) {
			response.headers().set(Names.WWW_AUTHENTICATE,
					"Basic realm=\"barchart.com\"");
			response.setStatus(HttpResponseStatus.UNAUTHORIZED);

		} else {
			try {

				final String[] structure = authHeader.split(" ");
				final String[] userpass =
						new String(Base64.decode(structure[1])).split(":");

				if (!authenticator.authenticate(userpass[0], userpass[1])) {
					response.headers().set(Names.WWW_AUTHENTICATE,
							"Basic realm=\"barchart.com\"");
					response.setStatus(HttpResponseStatus.UNAUTHORIZED);
				} else
					response.setStatus(HttpResponseStatus.ACCEPTED);

			} catch (final Exception e) {
			}
		}
	}
}
