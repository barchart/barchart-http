/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.servlet;

import static org.junit.Assert.assertEquals;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Servlet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.barchart.http.auth.Authenticator;
import com.barchart.http.request.RequestHandlerBase;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;
import com.barchart.http.server.HttpServer;
import com.barchart.http.server.HttpServerConfig;
import com.barchart.session.host.MultiThreadedRunner;

// MJS: We run it in parallel to stress the server more and also to take advantage of multiple cores for speed
@RunWith(MultiThreadedRunner.class)
public class TestHttpServer {

	private HttpServer server;
	private HttpClient client;

	// MJS: We need separate ports since we run in parallel
	private int port;

	private TestRequestHandler basic;
	private TestRequestHandler async;
	private TestRequestHandler asyncDelayed;
	private TestRequestHandler clientDisconnect;
	private TestRequestHandler error;
	private TestRequestHandler channelError;

	private TestAuthenticator testAuthenticator;

	@Before
	public void setUp() throws Exception {

		server = new HttpServer();

		basic = new TestRequestHandler("basic", false, 0, 0, false, false);
		async = new TestRequestHandler("async", true, 0, 0, false, false);
		asyncDelayed =
				new TestRequestHandler("async-delayed", true, 50, 0, false,
						false);
		clientDisconnect =
				new TestRequestHandler("client-disconnect", true, 500, 500,
						false, false);
		error = new TestRequestHandler("error", false, 0, 0, true, false);
		channelError =
				new TestRequestHandler("channel-error", false, 0, 0, false,
						true);

		try {
			final ServerSocket s = new ServerSocket(0);
			port = s.getLocalPort();

		} catch (final IOException e) {
			e.printStackTrace();
		}

		testAuthenticator = new TestAuthenticator();

		final HttpServerConfig config =
				new HttpServerConfig()
						.requestHandler("/basic", basic)
						.address(new InetSocketAddress("localhost", port))
						.parentGroup(new NioEventLoopGroup(1))
						.childGroup(new NioEventLoopGroup(1))

						// MJS: Request handlers are attached to resources

						.requestHandler("/async", async)
						.requestHandler("/async-delayed", asyncDelayed)
						.requestHandler("/client-disconnect", clientDisconnect)
						.requestHandler("/channel-error", channelError)
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
	public void testServlet() throws Exception {

		final HttpGet get = new HttpGet("http://localhost:" + port + "/basic");
		final HttpResponse response = client.execute(get);
		final String content =
				new BufferedReader(new InputStreamReader(response.getEntity()
						.getContent())).readLine().trim();

		assertEquals("basic", content);
	}

	// MJS: Authenticator we use for BASIC and DIGEST testing
	private class TestAuthenticator implements Authenticator {

		@Override
		public boolean authenticate(String username, String password) {
			return username.equals("aaa") && password.equals("bbb");
		}

		@Override
		public String getPassword(String username) {

			if (username.equals("aaa"))
				return "bbb";

			return null;
		}

	}

	private class TestServlet extends ServletWrapper {

		TestServlet(Servlet servlet_, ServletContextImpl context_) {
			super(servlet_, context_);
			// TODO Auto-generated constructor stub
		}

	}

	private class TestRequestHandler extends RequestHandlerBase {

		private final ScheduledExecutorService executor = Executors
				.newScheduledThreadPool(1);

		protected AtomicInteger requests = new AtomicInteger(0);

		protected ScheduledFuture<?> lastFuture;

		protected String content = null;
		protected boolean async = false;
		protected long execTime = 0;
		protected long writeTime = 0;
		protected boolean error = false;
		protected boolean disconnect = false;

		protected Map<String, List<String>> parameters;

		TestRequestHandler(final String content_, final boolean async_,
				final long execTime_, final long writeTime_,
				final boolean error_, final boolean disconnect_) {

			content = content_;
			async = async_;
			execTime = execTime_;
			writeTime = writeTime_;
			error = error_;
			disconnect = disconnect_;

		}

		@Override
		public void onRequest(final ServerRequest request,
				final ServerResponse response) throws IOException {

			requests.incrementAndGet();

			parameters = request.getParameters();

			final Runnable task = response(response);

			// response.setChunkedEncoding(true);

			if (async) {
				response.suspend();
				lastFuture =
						executor.schedule(task, execTime, TimeUnit.MILLISECONDS);
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

					if (disconnect) {
						try {
							response.finish().sync();
						} catch (final Exception e) {
							e.printStackTrace();
						}
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
