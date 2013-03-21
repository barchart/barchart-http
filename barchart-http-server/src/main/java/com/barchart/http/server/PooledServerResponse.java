/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.http.request.RequestHandler;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * Not thread safe.
 */
public class PooledServerResponse extends DefaultFullHttpResponse implements
		ServerResponse {

	private static final Logger log = LoggerFactory
			.getLogger(PooledServerResponse.class);

	final ServerMessagePool pool;

	private final Collection<Cookie> cookies = new HashSet<Cookie>();

	private ChannelHandlerContext context;
	private RequestHandler handler;
	private PooledServerRequest request;

	private OutputStream out;
	private Writer writer;

	private final Lock finishLock = new ReentrantLock();

	private Charset charSet = CharsetUtil.UTF_8;

	private boolean suspended = false;
	private boolean started = false;
	private boolean finished = false;
	private boolean closed = false;

	private long requestTime = 0;
	private RequestLogger logger;

	public PooledServerResponse(final ServerMessagePool pool_) {
		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		pool = pool_;
	}

	void init(final ChannelHandlerContext context_,
			final RequestHandler handler_, final PooledServerRequest request_,
			final RequestLogger logger_) {

		context = context_;
		handler = handler_;
		request = request_;
		logger = logger_;

		charSet = CharsetUtil.UTF_8;

		finished = false;
		suspended = false;
		closed = false;
		started = false;

		out = new ByteBufOutputStream(data());
		writer = new OutputStreamWriter(out, charSet);

		requestTime = System.currentTimeMillis();

	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public void setCookie(final Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public void setCookie(final String name, final String value) {
		cookies.add(new DefaultCookie(name, value));
	}

	@Override
	public void sendRedirect(final String location) {
		headers().set(HttpHeaders.Names.LOCATION, location);
	}

	@Override
	public PooledServerResponse setProtocolVersion(final HttpVersion version) {
		super.setProtocolVersion(version);
		return this;
	}

	@Override
	public PooledServerResponse setStatus(final HttpResponseStatus status) {
		super.setStatus(status);
		return this;
	}

	@Override
	public void setCharacterEncoding(final String charSet_) {
		charSet = Charset.forName(charSet_);
		writer = new OutputStreamWriter(out, charSet);
	}

	@Override
	public Charset getCharacterEncoding() {
		return charSet;
	}

	@Override
	public void setContentLength(final int length) {
		HttpHeaders.setContentLength(this, length);
	}

	@Override
	public void setContentType(final String mimeType) {
		headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeType);
	}

	@Override
	public boolean isChunkedEncoding() {
		return HttpHeaders.isTransferEncodingChunked(this);
	}

	@Override
	public void setChunkedEncoding(final boolean chunked) {

		if (chunked != isChunkedEncoding()) {

			if (chunked) {

				HttpHeaders.setTransferEncodingChunked(this);
				out = new HttpChunkOutputStream(context);
				writer = new OutputStreamWriter(out, charSet);

			} else {

				HttpHeaders.removeTransferEncodingChunked(this);
				out = new ByteBufOutputStream(data());
				writer = new OutputStreamWriter(out, charSet);

			}

		}

	}

	@Override
	public void write(final String data) throws IOException {
		write(data.getBytes());
	}

	@Override
	public void write(final byte[] data) throws IOException {

		checkFinished();

		out.write(data);
		out.flush();

	}

	@Override
	public void write(final byte[] data, final int offset, final int length)
			throws IOException {

		checkFinished();

		out.write(data, offset, length);
		out.flush();

	}

	@Override
	public long writtenBytes() {
		if (out instanceof ByteBufOutputStream) {
			return ((ByteBufOutputStream) out).writtenBytes();
		} else if (out instanceof HttpChunkOutputStream) {
			return ((HttpChunkOutputStream) out).writtenBytes();
		}
		return 0;
	}

	@Override
	public void suspend() {

		checkFinished();

		suspended = true;

	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	private ChannelFuture startResponse() {

		checkFinished();

		if (started) {
			throw new IllegalStateException("Response already started");
		}

		// Set headers
		headers().set(HttpHeaders.Names.SET_COOKIE,
				ServerCookieEncoder.encode(cookies));

		if (!isChunkedEncoding()) {
			setContentLength(data().readableBytes());
		}

		if (HttpHeaders.isKeepAlive(request)) {
			headers().set(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		}

		started = true;

		return context.write(this);

	}

	@Override
	public void finish() throws IOException {

		if (finishLock.tryLock()) {

			checkFinished();

			try {

				ChannelFuture writeFuture = null;

				// Handlers might call finish() on a cancelled/closed channel,
				// don't cause unnecessary pipeline exceptions
				if (context.channel().isOpen()) {

					if (isChunkedEncoding()) {

						if (!started) {
							log.debug("Warning, empty response");
							startResponse();
						}

						writeFuture =
								context.write(LastHttpContent.EMPTY_LAST_CONTENT);

					} else {

						writeFuture = startResponse();

					}

				}

				// Mark finished before resetting suspended to avoid
				// synchronization issue with channel handler auto-finish logic
				finished = true;
				suspended = false;

				if (writeFuture != null && !HttpHeaders.isKeepAlive(request)) {
					writeFuture.addListener(ChannelFutureListener.CLOSE);
				}

				// Record to access log
				logger.access(request, this, System.currentTimeMillis()
						- requestTime);

			} finally {
				close();
			}

		} else {

			throw new IllegalStateException(
					"ServerResponse has already finished");
		}

	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
		out.flush();
	}

	/**
	 * Closes this request to future interaction. This method is thread-safe,
	 * and calls the associated handler's onComplete() method, guaranteeing it
	 * is only called once per request.
	 */
	void close() {

		// Only the first call does anything, no need to wait if lock fails
		if (finishLock.tryLock()) {

			if (!closed) {

				try {

					closed = true;
					finished = true;
					suspended = false;
					started = false;

					if (handler != null) {
						handler.onComplete(request, this);
					}

					// Clear resources
					handler = null;
					context = null;

					out = null;
					writer = null;

				} finally {

					finishLock.unlock();

					pool.makeAvailable(request);
					request = null;

					pool.makeAvailable(this);

				}

			}

		}

	}

	ServerRequest request() {
		return request;
	}

	RequestHandler handler() {
		return handler;
	}

	private void checkFinished() {

		if (finished) {
			throw new IllegalStateException(
					"ServerResponse has already finished");
		}

	}

	/**
	 * Writes messages as HttpChunk objects to the client.
	 */
	private class HttpChunkOutputStream extends OutputStream {

		private final ByteBuf content = Unpooled.buffer();
		private final ChannelHandlerContext context;
		private long writtenBytes = 0;

		HttpChunkOutputStream(final ChannelHandlerContext context_) {
			context = context_;
		}

		/**
		 * Adds a single byte to the output buffer.
		 */
		@Override
		public void write(final int b) throws IOException {
			content.writeByte(b);
			writtenBytes++;
		}

		public long writtenBytes() {
			return writtenBytes;
		}

		@Override
		public void flush() {

			if (!started) {
				startResponse();
			}

			final HttpContent chunk = new DefaultHttpContent(content);
			context.write(chunk);

		}

	}

}
