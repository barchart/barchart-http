/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.handlers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;
import com.barchart.http.server.HttpServer;
import com.barchart.http.server.HttpServerConfig;
import com.barchart.util.test.CallableTest;

public class TestCancellableRequestHandler {

	HttpServer server;
	HttpClient client;

	TestRequestHandler basic;
	TestRequestHandler async;
	TestRequestHandler asyncDelayed;
	TestRequestHandler clientDisconnect;
	TestRequestHandler error;

	@Before
	public void setUp() throws Exception {

		server = new HttpServer();

		basic = new TestRequestHandler("basic", false, 0, 0, false);
		async = new TestRequestHandler("async", true, 0, 0, false);
		asyncDelayed =
				new TestRequestHandler("async-delayed", true, 50, 0, false);
		clientDisconnect =
				new TestRequestHandler("client-disconnect", true, 500, 500,
						false);
		error = new TestRequestHandler("error", false, 0, 0, true);

		final HttpServerConfig config =
				new HttpServerConfig().requestHandler("/basic", basic)
						.address(new InetSocketAddress("localhost", 8888))
						.parentGroup(new NioEventLoopGroup(1))
						.childGroup(new NioEventLoopGroup(1))
						.requestHandler("/async", async)
						.requestHandler("/async-delayed", asyncDelayed)
						.requestHandler("/client-disconnect", clientDisconnect)
						.requestHandler("/error", error).maxConnections(1);

		server.configure(config).listen().sync();

		client = new DefaultHttpClient(new PoolingClientConnectionManager());

	}

	@After
	public void tearDown() throws Exception {
		if (server.isRunning()) {
			server.shutdown().sync();
		}
	}

	@Test
	public void testCancellableRequest() throws Exception {

		final ScheduledExecutorService executor =
				Executors.newScheduledThreadPool(1);

		final HttpGet get =
				new HttpGet("http://localhost:8888/client-disconnect");

		executor.schedule(new Runnable() {

			@Override
			public void run() {
				get.abort();
			}

		}, 250, TimeUnit.MILLISECONDS);

		try {
			client.execute(get);
		} catch (final Exception e) {
		}

		CallableTest.waitFor(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return clientDisconnect.lastFuture != null
						&& clientDisconnect.lastFuture.isCancelled();
			}
		});

		assertNotNull(clientDisconnect.lastFuture);
		assertTrue(clientDisconnect.lastFuture.isCancelled());

	}

	private class TestRequestHandler extends CancellableRequestHandler {

		private final ScheduledExecutorService executor = Executors
				.newScheduledThreadPool(1);

		protected AtomicInteger requests = new AtomicInteger(0);

		protected ScheduledFuture<?> lastFuture;

		protected String content = null;
		protected boolean async = false;
		protected long execTime = 0;
		protected long writeTime = 0;
		protected boolean error = false;

		TestRequestHandler(final String content_, final boolean async_,
				final long execTime_, final long writeTime_,
				final boolean error_) {

			content = content_;
			async = async_;
			execTime = execTime_;
			writeTime = writeTime_;
			error = error_;

		}

		@Override
		public void onRequest(final ServerRequest request,
				final ServerResponse response) throws IOException {

			requests.incrementAndGet();

			final Runnable task = response(response);

			if (async) {
				response.suspend();
				lastFuture =
						executor.schedule(task, execTime, TimeUnit.MILLISECONDS);
				cancelOnAbort(request, response, lastFuture);
			} else {
				task.run();
			}

		}

		public Runnable response(final ServerResponse response) {

			return new Runnable() {

				@Override
				public void run() {

					if (error) {
						throw new RuntimeException("Uncaught exception");
					}

					try {
						response.write(content.getBytes());
						if (writeTime > 0) {
							try {
								Thread.sleep(writeTime);
							} catch (final InterruptedException e) {
							}
						}
						response.finish();
					} catch (final IOException e) {
						e.printStackTrace();
					}

				}

			};

		}

	}

}
