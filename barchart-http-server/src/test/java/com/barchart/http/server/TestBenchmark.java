/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.server;

import io.netty.channel.socket.nio.NioEventLoopGroup;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.barchart.http.request.RequestHandlerBase;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

public class TestBenchmark {

	public static void main(final String[] args) {

		final HttpServer server = new HttpServer();

		final HttpServerConfig config =
				new HttpServerConfig()
						.requestHandler("", new TestRequestHandler())
						.address(new InetSocketAddress("localhost", 8080))
						.parentGroup(new NioEventLoopGroup())
						.childGroup(new NioEventLoopGroup()).maxConnections(-1);

		try {
			server.configure(config).listen().sync();
			server.shutdownFuture().sync();
		} catch (final InterruptedException e) {
		}

	}

	private static class TestRequestHandler extends RequestHandlerBase {

		@Override
		public void onRequest(final ServerRequest request,
				final ServerResponse response) throws IOException {
			response.write("testing");
		}

	}

}
