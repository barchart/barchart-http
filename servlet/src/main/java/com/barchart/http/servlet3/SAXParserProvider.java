package com.barchart.http.servlet3;

/*
 * $Id: SAXParserProvider.java,v 1.15 2007/03/16 09:54:59 agoubard Exp $
 *
 * Copyright 2003-2007 Orange Nederland Breedband B.V.
 * See the COPYRIGHT file for redistribution and use restrictions.
 */

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Provider for <code>SAXParser</code> instances. This class will cache one
 * instance of a <code>SAXParser</code> per thread.
 * 
 * <p>
 * The returned <code>SAXParser</code> is guaranteed to be validating and
 * namespace-aware.
 * 
 * @version $Revision: 1.15 $ $Date: 2007/03/16 09:54:59 $
 * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
 * 
 * @since XINS 1.3.0
 */
public class SAXParserProvider {

	private static final Logger log = LoggerFactory
			.getLogger(SAXParserProvider.class);

	/**
	 * The factory for SAX parsers. This field is never <code>null</code>, it is
	 * initialized by a class initializer.
	 */
	private static final SAXParserFactory SAX_PARSER_FACTORY;

	/**
	 * The cache of <code>SAXParser</code> instances, one per thread. This field
	 * is never <code>null</code>.
	 */
	private static ThreadLocal CACHE;

	/**
	 * Initializes this class.
	 */
	static {
		SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
		SAX_PARSER_FACTORY.setNamespaceAware(true);
		SAX_PARSER_FACTORY.setValidating(false);

		CACHE = new ThreadLocal();
	}

	/**
	 * Creates a new <code>SAXParserProvider</code>.
	 */
	private SAXParserProvider() {
		// empty
	}

	/**
	 * Returns a <code>SAXParser</code> instance that can be used in the current
	 * thread. The <code>SAXParser</code> won't perform the validation of the
	 * XML.
	 * 
	 * @return a {@link SAXParser} instance, never <code>null</code>.
	 */
	public static SAXParser get() {
		Object o = CACHE.get();

		SAXParser parser;
		if (o == null) {
			parser = create();
			CACHE.set(parser);
		} else {
			parser = (SAXParser) o;
		}

		return parser;
	}

	/**
	 * Creates a new <code>SAXParser</code> instance.
	 * 
	 * @return a new {@link SAXParser} instance, never <code>null</code>.
	 */
	private static SAXParser create() {

		SAXParser parser;

		try {
			parser = SAX_PARSER_FACTORY.newSAXParser();
			parser.getXMLReader().setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String publicId,
						String systemId) {
					return new InputSource(
							new ByteArrayInputStream(new byte[0]));
				}
			});
		} catch (Exception exception) {

			log.warn("Exception", exception);
			String exceptionMessage = exception.getMessage();
			String message;
			if (exceptionMessage == null) {
				message = "Error when creating a SAX parser.";
			} else {
				message =
						"Error when creating a SAX parser: \""
								+ exceptionMessage + "\".";
			}
			RuntimeException e = new RuntimeException(message);
			throw e;
		}
		return parser;
	}
}