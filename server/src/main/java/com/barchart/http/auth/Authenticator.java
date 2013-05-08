package com.barchart.http.auth;

public interface Authenticator {

	// MJS: In some authentication shemes we can provide an actual password
	public boolean authenticate(String username, String password);

	// MJS: This is needed to match the hashed client response
	// https://tools.ietf.org/html/rfc2617#section-4.13
	// i.e for digest we provide then HA1 hash associated with that username
	// The server needs to protect that DB of username-hashes but at least the
	// password stays safe
	public String getData(String username);

}