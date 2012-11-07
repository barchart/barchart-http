package com.barchart.http.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.netty.channel.socket.nio.NioEventLoopGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.barchart.http.api.ServerRequest;
import com.barchart.http.api.ServerResponse;
import com.barchart.util.thread.test.CallableTest;

public class TestHttpServer {

	HttpServer server;
	HttpClient client;

	TestRequestHandler basic;
	TestRequestHandler async;
	TestRequestHandler asyncDelayed;
	TestRequestHandler clientDisconnect;

	@Before
	public void setUp() throws Exception {

		server = new HttpServer();

		basic = new TestRequestHandler("basic", false, 0);
		async = new TestRequestHandler("async", true, 0);
		asyncDelayed = new TestRequestHandler("async-delayed", true, 50);
		clientDisconnect =
				new TestRequestHandler("client-disconnect", true, 5000);

		server.addHandler("/basic", basic);
		server.addHandler("/async", async);
		server.addHandler("/async-delayed", asyncDelayed);
		server.addHandler("/client-disconnect", clientDisconnect);

		server.listen(new InetSocketAddress("localhost", 8080),
				new NioEventLoopGroup(1), new NioEventLoopGroup(1)).sync();

		client = new DefaultHttpClient();

	}

	@After
	public void tearDown() throws Exception {
		server.shutdown().sync();
	}

	@Test
	public void testBasicRequest() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:8080/basic");
		final HttpResponse response = client.execute(get);
		final String content =
				new BufferedReader(new InputStreamReader(response.getEntity()
						.getContent())).readLine().trim();

		assertEquals("basic", content);

	}

	@Test
	public void testAsyncRequest() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:8080/async");
		final HttpResponse response = client.execute(get);
		final String content =
				new BufferedReader(new InputStreamReader(response.getEntity()
						.getContent())).readLine().trim();

		assertNotNull(async.lastFuture);
		assertFalse(async.lastFuture.isCancelled());
		assertEquals("async", content);

	}

	@Test
	public void testAsyncDelayedRequest() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:8080/async-delayed");
		final HttpResponse response = client.execute(get);
		final String content =
				new BufferedReader(new InputStreamReader(response.getEntity()
						.getContent())).readLine().trim();

		assertNotNull(asyncDelayed.lastFuture);
		assertFalse(asyncDelayed.lastFuture.isCancelled());
		assertEquals("async-delayed", content);

	}

	@Test
	public void testCancellableRequest() throws Exception {

		final ScheduledExecutorService executor =
				Executors.newScheduledThreadPool(1);

		final HttpGet get =
				new HttpGet("http://localhost:8080/client-disconnect");

		executor.schedule(new Runnable() {

			@Override
			public void run() {
				get.abort();
			}

		}, 500, TimeUnit.MILLISECONDS);

		try {
			final HttpResponse response = client.execute(get);
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
		protected long wait = 0;

		TestRequestHandler(final String content_, final boolean async_,
				final long wait_) {

			content = content_;
			async = async_;
			wait = wait_;

		}

		@Override
		public void onRequest(final ServerRequest request,
				final ServerResponse response) throws IOException {

			requests.incrementAndGet();

			final Runnable task = response(response, content);

			if (async) {
				response.suspend();
				lastFuture =
						executor.schedule(task, wait, TimeUnit.MILLISECONDS);
				cancelOnAbort(request, response, lastFuture);
			} else {
				task.run();
			}

		}

		public Runnable response(final ServerResponse response,
				final String content) {

			return new Runnable() {

				@Override
				public void run() {
					try {
						response.write(content.getBytes());
						response.finish();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}

			};

		}

	}

}
