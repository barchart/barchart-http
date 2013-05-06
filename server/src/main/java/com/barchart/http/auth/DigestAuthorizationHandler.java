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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

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

	/**
	 * Returns the request body as String
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private String readRequestBody(ServerRequest request) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader =
						new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		String body = stringBuilder.toString();
		return body;
	}

	private String getAuthenticateHeader() {
		String header = "";

		header += "Digest realm=\"" + realm + "\",";
		header += "qop=" + authMethod + ",";
		header += "nonce=\"" + nonce + "\",";
		header += "opaque=\"" + getOpaque(realm, nonce) + "\"";

		return header;
	}

	@Override
	public void authenticate(ServerRequest request, ServerResponse response)
			throws IOException {

		String requestBody = readRequestBody(request);

		final String authHeader = request.headers().get("Authorization");

		if (authHeader == null || "".equals(authHeader)) {
			response.headers().set(Names.WWW_AUTHENTICATE,
					getAuthenticateHeader());
			response.setStatus(HttpResponseStatus.UNAUTHORIZED);

		} else {
			if (authHeader.startsWith("Digest")) {

				// parse the values of the Authentication header into a
				// hashmap
				final HashMap<String, String> headerValues =
						parseHeader(authHeader);

				final String userName = headerValues.get("username");

				final String method = request.getMethod().name();

				final String ha1 =
						DigestUtils.md5Hex(userName + ":" + realm + ":"
								+ authenticator.getPassword(userName));

				String qop = headerValues.get("qop");

				String ha2;

				String reqURI = headerValues.get("uri");

				if (qop != null && qop.equals("auth-int")) {
					String entityBodyMd5 = DigestUtils.md5Hex(requestBody);
					ha2 =
							DigestUtils.md5Hex(method + ":" + reqURI + ":"
									+ entityBodyMd5);
				} else {
					ha2 = DigestUtils.md5Hex(method + ":" + reqURI);
				}

				String serverResponse;

				if (qop == null || "".equals(qop)) {
					serverResponse =
							DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + ha2);

				} else {
					String nonceCount = headerValues.get("nc");
					String clientNonce = headerValues.get("cnonce");

					serverResponse =
							DigestUtils.md5Hex(ha1 + ":" + nonce + ":"
									+ nonceCount + ":" + clientNonce + ":"
									+ qop + ":" + ha2);

				}

				String clientResponse = headerValues.get("response");

				if (!serverResponse.equals(clientResponse)) {
					response.headers().set(Names.WWW_AUTHENTICATE,
							getAuthenticateHeader());
					response.setStatus(HttpResponseStatus.UNAUTHORIZED);
				}
			} else {
				response.setStatus(HttpResponseStatus.UNAUTHORIZED);
			}
		}
	}
}
