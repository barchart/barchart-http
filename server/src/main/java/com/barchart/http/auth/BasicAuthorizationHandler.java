/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.auth;

import com.barchart.http.util.Base64;

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

	@Override
	public String authorize(final String data) {

		try {

			final String[] structure = data.split(" ");
			final String[] userpass =
					new String(Base64.decode(structure[1])).split(":");

			if (authenticator.authenticate(userpass[0], userpass[1])) {
				return userpass[0];
			}

		} catch (final Exception e) {
		}

		return null;

	}

	// MJS: Basic doesn't require a challenge

	@Override
	public String getAuthenticateHeader(String data) {
		return null;
	}

}
