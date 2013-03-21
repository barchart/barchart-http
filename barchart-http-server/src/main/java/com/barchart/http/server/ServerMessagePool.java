package com.barchart.http.server;

import java.util.concurrent.Callable;

/**
 * HTTP request/response object pool for low-garbage request handling
 */
public class ServerMessagePool {

	private final ObjectPool<PooledServerRequest> requestPool;
	private final ObjectPool<PooledServerResponse> responsePool;

	/**
	 * Create a new fixed-size message pool.
	 * 
	 * @param maxObjects_
	 *            The pool size, or -1 for unlimited
	 */
	public ServerMessagePool(final int maxObjects_) {

		requestPool =
				new ObjectPool<PooledServerRequest>(maxObjects_,
						new Callable<PooledServerRequest>() {
							@Override
							public PooledServerRequest call() throws Exception {
								return new PooledServerRequest();
							}
						});

		responsePool =
				new ObjectPool<PooledServerResponse>(maxObjects_,
						new Callable<PooledServerResponse>() {
							@Override
							public PooledServerResponse call() throws Exception {
								return new PooledServerResponse(
										ServerMessagePool.this);
							}
						});

	}

	public PooledServerRequest getRequest() throws InterruptedException {
		return requestPool.take();
	}

	public PooledServerResponse getResponse() throws InterruptedException {
		return responsePool.take();
	}

	void makeAvailable(final PooledServerRequest request) {
		requestPool.give(request);
	}

	void makeAvailable(final PooledServerResponse response) {
		responsePool.give(response);
	}
}
