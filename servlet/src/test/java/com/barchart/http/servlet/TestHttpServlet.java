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
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.CharBuffer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.barchart.http.server.HttpServer;
import com.barchart.http.server.HttpServerConfig;
import com.barchart.session.host.MultiThreadedRunner;

// MJS: We run it in parallel to stress the server more and also to take advantage of multiple cores for speed
@RunWith(MultiThreadedRunner.class)
public class TestHttpServlet {

	private HttpServer server;
	private HttpClient client;

	// MJS: We need separate ports since we run in parallel
	private int port;

	private TestServlet servlet;

	@Before
	public void setUp() throws Exception {

		server = new HttpServer();

		// MJS: Best way to access a resource for unit testing I found so far
		URL url =
				this.getClass().getResource(
						File.separator + "barchart-servlet-example.war");

		File f = new File(url.getFile().replace("%20", " "));

		servlet =
				new TestServlet(f, "com.barchart.servlet.example.TestServlet");

		port = 50000;

		final HttpServerConfig config =
				new HttpServerConfig()
						.address(new InetSocketAddress("localhost", port))
						.parentGroup(new NioEventLoopGroup(1))
						.childGroup(new NioEventLoopGroup(1))

						// MJS: Attach the servlet
						.requestHandler("/servlet", servlet);

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

		final HttpGet get =
				new HttpGet("http://localhost:" + port + "/servlet");
		final HttpResponse response = client.execute(get);
		final CharBuffer buffer = CharBuffer.allocate(10000);
		new BufferedReader(new InputStreamReader(response.getEntity()
				.getContent())).read(buffer);

		assertEquals("basic", buffer);
	}

	private class TestServlet extends ServletWrapper {

		TestServlet(File warFile, String className) {
			super(warFile, className);
		}

	}
}
