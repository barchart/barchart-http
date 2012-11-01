package com.barchart.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

import com.barchart.http.api.ServerResponse;

public class ServerResponseImpl extends DefaultHttpResponse implements
		ServerResponse {

	private final ChannelHandlerContext context;
	private final ByteBuf content = Unpooled.buffer();
	private final OutputStream out;

	private Charset charSet = CharsetUtil.UTF_8;
	private Writer writer;
	private boolean suspended = false;
	private boolean finished = false;
	private final Collection<Cookie> cookies = new HashSet<Cookie>();

	ServerResponseImpl(final ChannelHandlerContext context_) {

		super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		context = context_;

		out = new ByteBufOutputStream(content);
		writer = new OutputStreamWriter(out, CharsetUtil.UTF_8);

		super.setContent(content);

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
	public void setCookie(final String name, final String value) {
		cookies.add(new DefaultCookie(name, value));
	}

	@Override
	public void sendRedirect(final String location) {
		setHeader("Location", location);
	}

	@Override
	public void setCharacterEncoding(final String charSet_) {
		charSet = Charset.forName(charSet_);
		writer = new OutputStreamWriter(out, charSet);
	}

	@Override
	public void setContentLength(final int length) {
		setHeader("Content-Length", length);
	}

	@Override
	public void setContentType(final String mimeType) {
		setHeader("Content-Type", mimeType);
	}

	@Override
	public void write(final byte[] data) throws IOException {
		out.write(data);
	}

	@Override
	public void write(final byte[] data, final int offset, final int length)
			throws IOException {
		out.write(data, offset, length);
	}

	@Override
	public void suspend() {
		suspended = true;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void finish() {

		suspended = false;
		finished = true;

		// Set headers
		setHeader("Set-Cookie", ServerCookieEncoder.encode(cookies));

		context.write(this);

	}

	@Override
	public boolean isFinished() {
		return finished;
	}

}
