package cn.bromine0x23.sgip.windowing;

/*
 * #%L
 * ch-commons-util
 * %%
 * Copyright (C) 2012 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import cn.bromine0x23.sgip.util.UnwrappedWeakReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Internal utility class to monitor the window and send events upstream
 * to listeners.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class WindowMonitor<TKey, TRequest, TResponse> implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Window.class);

	private final WeakReference<Window<TKey, TRequest, TResponse>> windowReference;
	private final String                                           monitorThreadName;

	public WindowMonitor(Window<TKey, TRequest, TResponse> window, String monitorThreadName) {
		this.windowReference = new WeakReference<>(window);
		this.monitorThreadName = monitorThreadName;
	}

	@Override
	public void run() {
		String currentThreadName = null;
		try {
			if (monitorThreadName != null) {
				Thread currentThread = Thread.currentThread();
				currentThreadName = currentThread.getName();
				currentThread.setName(monitorThreadName);
			}
			Window<TKey, TRequest, TResponse> window = windowReference.get();

			// check if the window using this monitor was GC'ed
			if (window == null) {
				logger.error("The parent Window was garbage collected in this WindowMonitor(): missing call to Window.reset() to stop this monitoring thread (will throw exception to cancel this recurring execution!)");
				throw new IllegalStateException("Parent Window was garbage collected (missing call to Window.reset() somewhere in code)");
			}

			if (logger.isTraceEnabled()) {
				logger.trace("Monitor running... (current window.size [" + window.getSize() + "])");
			}

			List<WindowFuture<TKey, TRequest, TResponse>> expired = window.cancelAllExpired();
			if (expired != null && expired.size() > 0) {
				if (logger.isTraceEnabled()) {
					logger.trace("Monitor found [" + expired.size() + "] requests expired");
				}
				// process each expired request and pass up the chain to handlers
				for (WindowFuture<TKey, TRequest, TResponse> future : expired) {
					for (UnwrappedWeakReference<WindowListener<TKey, TRequest, TResponse>> listenerRef : window.getListeners()) {
						WindowListener<TKey, TRequest, TResponse> listener = listenerRef.get();
						if (listener == null) {
							// remove this reference from our array (no good anymore)
							window.removeListener(listener);
						} else {
							try {
								listener.expired(future);
							} catch (Throwable t) {
								logger.error("Ignoring uncaught exception thrown in listener: ", t);
							}
						}
					}
				}
			}
		} finally {
			if (currentThreadName != null) {
				// change the name of the thread back
				Thread.currentThread().setName(currentThreadName);
			}
		}
	}
}
