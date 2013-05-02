/*
 * $Id: XINSHttpSession.java,v 1.13 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet3;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * A user session.
 * 
 * @version $Revision: 1.13 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * 
 * @since XINS 1.4.0
 * 
 * @author Maurycy - modified for Netty 4.0.0 and servlet API 3.0
 * 
 */
public class HttpSessionWrapper implements HttpSession {

	/**
	 * The random generator.
	 */
	private final static Random RANDOM = new Random();

	/**
	 * The session attributes.
	 */
	private final Hashtable _attributes = new Hashtable();

	/**
	 * The creation time of the session.
	 */
	private final long _creationTime = System.currentTimeMillis();

	/**
	 * The ID of the session.
	 */
	private final int _sessionID = RANDOM.nextInt();

	/**
	 * Creates a new instance of XINSHttpSession.
	 */
	HttpSessionWrapper() {
	}

	@Override
	public void removeValue(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String name) {
		_attributes.remove(name);
	}

	@Override
	public Object getAttribute(String name) {
		return _attributes.get(name);
	}

	@Override
	public Object getValue(String name) {
		return getAttribute(name);
	}

	@Override
	public void setMaxInactiveInterval(int i) {
	}

	@Override
	public void setAttribute(String name, Object value) {
		_attributes.put(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	@Override
	public Enumeration getAttributeNames() {
		return _attributes.keys();
	}

	@Override
	public long getCreationTime() {
		return _creationTime;
	}

	@Override
	public String getId() {
		return "" + _sessionID;
	}

	@Override
	public long getLastAccessedTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getMaxInactiveInterval() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletContext getServletContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getValueNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void invalidate() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNew() {
		throw new UnsupportedOperationException();
	}
}
