package com.barchart.http.server;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.barchart.http.api.HandlerURIMapping;
import com.barchart.http.api.RequestHandler;
import com.barchart.http.api.RequestHandlerFactory;
import com.barchart.http.api.RequestURLMapper;

public class DefaultURLMapper implements RequestURLMapper {

	private final Map<String, Object> handlers =
			new ConcurrentSkipListMap<String, Object>(new Comparator<String>() {

				// Sort by reverse length first to allow overriding parent
				// mappings
				@Override
				public int compare(final String o1, final String o2) {

					final int l1 = o1.length();
					final int l2 = o2.length();

					if (l1 < l2) {
						return 1;
					} else if (l2 < l1) {
						return -1;
					} else {
						return o1.compareTo(o2);
					}

				}

			});

	private final Map<String, RequestHandlerFactory> factories =
			new ConcurrentHashMap<String, RequestHandlerFactory>();

	@Override
	public HandlerURIMapping getHandlerFor(final String uri) {

		for (final Map.Entry<String, Object> entry : handlers.entrySet()) {

			if (uri.startsWith(entry.getKey())) {

				if (entry.getValue() instanceof RequestHandler) {
					return new HandlerURIMapping(entry.getKey(),
							(RequestHandler) entry.getValue());
				}

				if (entry.getValue() instanceof RequestHandlerFactory) {
					return new HandlerURIMapping(entry.getKey(),
							((RequestHandlerFactory) entry.getValue())
									.newHandler());
				}

			}

		}

		return null;

	}

	public void addHandler(final String path, final RequestHandler handler) {
		handlers.put(path, handler);
	}

	public void addHandler(final String path,
			final RequestHandlerFactory factory) {
		factories.put(path, factory);
	}

}
