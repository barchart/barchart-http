package com.barchart.http.auth;

public interface Authenticator {

	public boolean authenticate(String username, String password);

	// MJS: This is needed to match the hashed client response
	// https://tools.ietf.org/html/rfc2617#section-4.13 describes a better
	// scheme than this as passwords need to be protected but will expand on
	// it later
	public String getPassword(String username);

}