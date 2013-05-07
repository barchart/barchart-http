/*
 * $Id: LocalServletConfig.java,v 1.21 2010/09/29 17:21:48 agoubard Exp $
 *
 * See the COPYRIGHT file for redistribution and use restrictions.
 */
package com.barchart.http.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is an implementation of the ServletConfig that can be called
 * locally.
 * 
 * @version $Revision: 1.21 $ $Date: 2010/09/29 17:21:48 $
 * @author <a href="mailto:anthony.goubard@japplis.com">Anthony Goubard</a>
 * 
 * @author Maurycy - modified for Netty 4.0.0 and servlet API 3.0
 * 
 */
public class LocalServletConfig implements ServletConfig {

	private static final Logger log = LoggerFactory
			.getLogger(LocalServletConfig.class);

	/**
	 * The name of the servlet.
	 */
	private String _servletName;

	/**
	 * The class of the servlet.
	 */
	private String _servletClass;

	/**
	 * The properties of the servlet.
	 */
	private final Properties _initParameters;

	/**
	 * The servlet context.
	 */
	private final ServletContext _context;

	/**
	 * The WAR file.
	 */
	private final File _warFile;

	/**
	 * Creates a new Servlet configuration.
	 * 
	 * @param warFileLocation
	 *            the war file containing the servlet to deploy, cannot be
	 *            <code>null</code>.
	 */
	public LocalServletConfig(File warFileLocation) {
		_warFile = warFileLocation;
		_initParameters = new Properties();
		_context = new HttpServletContextWrapper(this);

		try {
			JarFile warFile = new JarFile(warFileLocation);
			JarEntry webxmlEntry = warFile.getJarEntry("WEB-INF/web.xml");

			// MJS: Sometimes we get sun-web as opposed to web.xml
			if (webxmlEntry == null)
				webxmlEntry = warFile.getJarEntry("WEB-INF/sun-web.xml");

			InputStream webxmlInputStream = warFile.getInputStream(webxmlEntry);
			parseWebXML(webxmlInputStream);
		} catch (Exception ex) {

			log.warn("Exception", ex);
		}
	}

	/**
	 * Parses the web.xml file.
	 * 
	 * @param webxmlInputStream
	 *            the web.xml file input stream.
	 * 
	 * @throws Exception
	 *             if the file cannot be parsed for any reason.
	 */
	private void parseWebXML(InputStream webxmlInputStream) throws Exception {
		DefaultHandler handler = new WebInfoParser();
		SAXParserProvider.get().parse(webxmlInputStream, handler);
		webxmlInputStream.close();
	}

	@Override
	public String getInitParameter(String param) {
		return _initParameters.getProperty(param);
	}

	@Override
	public String getServletName() {
		return _servletName;
	}

	/**
	 * Gets the class name of the Servlet.
	 * 
	 * @return the class name of the servlet, cannot be <code>null</code>.
	 */
	public String getServletClass() {
		return _servletClass;
	}

	@Override
	public ServletContext getServletContext() {
		return _context;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return _initParameters.keys();
	}

	/**
	 * Gets the WAR file location.
	 * 
	 * @return the WAR file, never <code>null</code>
	 */
	File getWarFile() {
		return _warFile;
	}

	/**
	 * Parser for the web.xml containing the information about the Servlet.
	 */
	private class WebInfoParser extends DefaultHandler {
		/**
		 * The PCDATA element of the tag that is actually parsed.
		 */
		private StringBuffer _pcdata;

		/**
		 * The name of the property that is currently parsed.
		 */
		private String _paramName;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws IllegalArgumentException,
				SAXException {
			_pcdata = new StringBuffer(80);
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws IllegalArgumentException, SAXException {
			if (qName.equals("param-name")) {
				_paramName = _pcdata.toString();
			} else if (qName.equals("param-value")) {
				_initParameters.setProperty(_paramName, _pcdata.toString());
			} else if (qName.equals("servlet-name")) {
				_servletName = _pcdata.toString();
			} else if (qName.equals("servlet-class")) {
				_servletClass = _pcdata.toString();
			}
			_pcdata = null;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws IndexOutOfBoundsException {

			if (_pcdata != null) {
				_pcdata.append(ch, start, length);
			}
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId) {
			return new InputSource(new ByteArrayInputStream(new byte[0]));
		}
	}
}
