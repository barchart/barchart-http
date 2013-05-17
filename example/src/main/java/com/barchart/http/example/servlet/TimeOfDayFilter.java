/*
 * Example to test barchart-http
 * 
 * Copyright 2013 Barchart Inc.
 * 
 */

package com.barchart.http.example.servlet;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * 
 * @author maurycy
 */
@WebFilter(filterName = "TimeOfDayFilter", urlPatterns = { "/*" }, initParams = { @WebInitParam(name = "state", value = "awake") })
public class TimeOfDayFilter implements Filter {
	String state = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		state = filterConfig.getInitParameter("state");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		Calendar cal = GregorianCalendar.getInstance();

		switch (cal.get(Calendar.HOUR_OF_DAY)) {
			case 22:
			case 23:
			case 24:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				state = "sleepy";

				break;

			case 8:
			case 13:
			case 18:
				state = "hungry";

				break;

			case 9:
			case 10:
			case 11:
			case 12:
			case 14:
			case 16:
			case 17:
				state = "alert";

				break;

			case 15:
				state = "in need of coffee";

				break;

			case 19:
			case 20:
				state = "content";

				break;

			case 21:
				state = "lethargic";

				break;
		}

		req.setAttribute("state", state);
		chain.doFilter(req, res);
	}

	@Override
	public void destroy() {
	}
}
