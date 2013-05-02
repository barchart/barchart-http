/*
 * Example to test barchart-http
 * 
 * Copyright 2013 Barchart Inc.
 * 
 */
package com.barchart.servlet.example;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 * 
 * @author maurycy
 */
@WebListener()
public class SimpleServletListener implements ServletContextListener,
		ServletContextAttributeListener {

	private static final Logger log = LoggerFactory
			.getLogger(SimpleServletListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		log.info("Context initiallized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		log.info("Context destroyed");
	}

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		log.info("Attribute " + event.getName()
				+ " has been added, with value: " + event.getValue());
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		log.info("Attribute " + event.getName() + " has been removed");
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		log.info("Attribute " + event.getName()
				+ " has been replaced, with value: " + event.getValue());
	}
}
