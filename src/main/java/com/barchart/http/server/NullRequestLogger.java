package com.barchart.http.server;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

public class NullRequestLogger implements RequestLogger {

	@Override
	public void access(ServerRequest request, ServerResponse response,
			long duration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(ServerRequest request, ServerResponse response,
			Throwable exception) {
		// TODO Auto-generated method stub

	}

}
