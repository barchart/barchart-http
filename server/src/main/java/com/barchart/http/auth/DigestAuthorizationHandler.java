/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.auth;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Implements the HTTP Digest Auth as per RFC2617.
 * 
 * @author maurycy
 * 
 */
public class DigestAuthorizationHandler implements AuthorizationHandler {

	private final String authMethod = "auth";
	private final String realm = "barchart.com";

	// MJS: This is how we calculate the nonce
	public String nonce;
	public ScheduledExecutorService nonceRefreshExecutor;

	private final Authenticator authenticator;
	private final SecureRandom random = new SecureRandom();

	public DigestAuthorizationHandler(final Authenticator authenticator_) {
		authenticator = authenticator_;

		nonce = calculateNonce();
		nonceRefreshExecutor = Executors.newScheduledThreadPool(1);

		nonceRefreshExecutor.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				nonce = calculateNonce();
			}
		}, 1, 1, TimeUnit.MINUTES);
	}

	@Override
	public String getMethod() {
		return "Digest";
	}

	/**
	 * Gets the Authorization header string minus the "AuthType" and returns a
	 * hashMap of keys and values
	 */
	private HashMap<String, String> parseHeader(String headerString) {

		String headerStringWithoutScheme =
				headerString.substring(headerString.indexOf(" ") + 1).trim();

		HashMap<String, String> values = new HashMap<String, String>();

		String keyValueArray[] = headerStringWithoutScheme.split(",");

		for (String keyval : keyValueArray) {

			if (keyval.contains("=")) {
				String key = keyval.substring(0, keyval.indexOf("="));
				String value = keyval.substring(keyval.indexOf("=") + 1);
				values.put(key.trim(), value.replaceAll("\"", "").trim());
			}
		}

		return values;
	}

	/**
	 * Calculate the nonce based on current time-stamp upto the second, and a
	 * random seed
	 */
	public String calculateNonce() {
		Date d = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
		String fmtDate = f.format(d);

		Integer randomInt = random.nextInt();
		return DigestUtils.md5Hex(fmtDate + randomInt.toString());
	}

	private String getOpaque(String domain, String nonce) {
		return DigestUtils.md5Hex(domain + nonce);
	}

	// MJS: This is needed for validation
	private String requestBody;

	/**
	 * @return the requestBody
	 */
	public String getRequestBody() {
		return requestBody;
	}

	/**
	 * @param requestBody
	 *            the requestBody to set
	 */
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	// MJS: We pass on the authorization header string here and we compute an
	// expected response also including a nonce to prevent repeat attacks

	@Override
	public String authorize(final String data) {

		HashMap<String, String> headerValues = parseHeader(data);

		String userName = headerValues.get("username");

		String ha1 =
				DigestUtils.md5Hex(userName + ":" + realm + ":"
						+ authenticator.getPassword(userName));

		String qop = headerValues.get("qop");

		String ha2;

		String reqURI = headerValues.get("uri");

		if (!qop.isEmpty() && qop.equals("auth-int")) {
			String entityBodyMd5 = DigestUtils.md5Hex(requestBody);
			ha2 =
					DigestUtils.md5Hex(getMethod() + ":" + reqURI + ":"
							+ entityBodyMd5);
		} else {
			ha2 = DigestUtils.md5Hex(getMethod() + ":" + reqURI);
		}

		String serverResponse;

		if (qop.isEmpty()) {
			serverResponse = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + ha2);

		} else {
			String nonceCount = headerValues.get("nc");
			String clientNonce = headerValues.get("cnonce");

			serverResponse =
					DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + nonceCount
							+ ":" + clientNonce + ":" + qop + ":" + ha2);
		}

		String clientResponse = headerValues.get("response");

		// MJS: Since the password is already hashed
		if (!serverResponse.equals(clientResponse))
			return clientResponse;

		return null;
	}

	public interface Authenticator {

		public boolean authenticate(String username, String password);

		// MJS: This is needed to match the hashed client response
		public String getPassword(String username);

	}

	@Override
	public String getAuthenticateHeader(String data) {
		String header = "";

		header += "Digest realm=\"" + realm + "\",";
		header += "qop=" + authMethod + ",";
		header += "nonce=\"" + nonce + "\",";
		header += "opaque=\"" + getOpaque(realm, nonce) + "\"";

		return header;
	}

}
