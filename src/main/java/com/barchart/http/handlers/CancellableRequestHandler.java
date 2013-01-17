package com.barchart.http.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barchart.http.request.RequestAttribute;
import com.barchart.http.request.RequestAttributeKey;
import com.barchart.http.request.ServerRequest;
import com.barchart.http.request.ServerResponse;

/**
 * Base class for stateless request handlers that want to register Futures that
 * will be cancelled when the client disconnects.
 */
public abstract class CancellableRequestHandler extends RequestHandlerBase {

	private static final Logger log = LoggerFactory
			.getLogger(CancellableRequestHandler.class);

	private final RequestAttributeKey<CancelFutureList> ATTR_CANCEL_TASKS =
			new RequestAttributeKey<CancelFutureList>("cancel-tasks");

	@Override
	public void onAbort(final ServerRequest request,
			final ServerResponse response) {

		cancelTasks(request);

	}

	@Override
	public void onException(final ServerRequest request,
			final ServerResponse response, final Throwable exception) {

		if (log.isTraceEnabled()) {
			log.trace(
					"Request encountered an uncaught exception, cancelling tasks",
					exception);
		} else {
			log.debug("Request encountered an uncaught exception, cancelling tasks: "
					+ exception.getMessage());
		}

		cancelTasks(request);

	}

	protected synchronized void cancelTasks(final ServerRequest request) {

		synchronized (ATTR_CANCEL_TASKS) {

			final List<Future<?>> tasks = request.attr(ATTR_CANCEL_TASKS).get();

			if (tasks != null) {
				for (final Future<?> future : tasks) {
					if (!future.isDone()) {
						future.cancel(true);
					}
				}
			}

		}

	}

	protected synchronized void cancelOnAbort(final ServerRequest request,
			final ServerResponse response, final Future<?> future) {

		synchronized (ATTR_CANCEL_TASKS) {

			final RequestAttribute<CancelFutureList> attr =
					request.attr(ATTR_CANCEL_TASKS);
			CancelFutureList tasks = attr.get();

			if (tasks == null) {
				tasks = new CancelFutureList();
				attr.set(tasks);
			}

			tasks.add(future);

		}

	}

	private class CancelFutureList extends ArrayList<Future<?>> {
		private static final long serialVersionUID = 1L;
	}

}
