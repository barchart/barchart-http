package com.barchart.http.server;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

public class NullRequestLogger implements RequestLogger {

	@Override
	public void access(final ServerRequest request,
			final ServerResponse response, final long duration) {
		// Noop
	}

	@Override
	public void error(final ServerRequest request,
			final ServerResponse response, final Throwable exception) {
		// Noop
	}

}
