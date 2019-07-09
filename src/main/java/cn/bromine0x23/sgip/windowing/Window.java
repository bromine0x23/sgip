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
package cn.bromine0x23.sgip.windowing;

import cn.bromine0x23.sgip.util.UnwrappedWeakReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A utility class to support "windowed" protocols that permit requests to be
 * sent asynchronously and the responses to be processed at a later time.
 * Responses may be returned in a different order than requests were sent.
 * <br><br>
 * Windowed protocols generally provide high throughput over high latency
 * links such as TCP/IP connections since they allow requests one after the
 * other without waiting for a response before sending the next request. This
 * allows the underlying TCP/IP socket to potentially buffer multiple requests
 * in one packet.
 * <br><br>
 * The "window" is the amount of unacknowledged requests that are permitted to
 * be outstanding/unacknowledged at any given time.  This implementation
 * allows a max window size to be defined during construction.  This represents
 * the number of open "slots".  When a response is received, it's up to the
 * user of this class to make sure that response is added so that any threads
 * waiting for a response are properly signaled.
 * <br><br>
 * The life cycle of a request in a Window has 3 steps:
 * <ol>
 * <li>Request offered<br>
 * <ul>
 * <li>If free slot exists then goto 2</li>
 * <li>If no free slot exists, offer now "pending" and block for specified time.  May either timeout or if free slot opens, then goto 2</li>
 * </ul>
 * </li>
 * <li>Request accepted (caller may optionally await() on returned future till completion)</li>
 * <li>Request completed/done (either success, failure, or cancelled)</li>
 * </ol>
 * <br><br>
 * If monitoring is enabled, it's very important to call "freeExternalResources()" if a
 * Window will no longer be used.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Window<TKey, TRequest, TResponse> {

	private static final Logger logger = LoggerFactory.getLogger(Window.class);

	private final int                                                                                     maxSize;
	private final ConcurrentHashMap<TKey, DefaultWindowFuture<TKey, TRequest, TResponse>>                 futures;
	private final ReentrantLock                                                                           lock;
	private final Condition                                                                               completedCondition;
	// number of threads waiting to offer a request to be accepted
	private       AtomicInteger                                                                           pendingOffers;
	private       AtomicBoolean                                                                           pendingOffersAborted;
	// for scheduling tasks (such as expiring requests)
	private final ScheduledExecutorService                                                                executor;
	private       ScheduledFuture<?>                                                                      monitorHandle;
	private final WindowMonitor<TKey, TRequest, TResponse>                                                monitor;
	private final long                                                                                    monitorInterval;
	private final CopyOnWriteArrayList<UnwrappedWeakReference<WindowListener<TKey, TRequest, TResponse>>> listeners;

	/**
	 * Creates a new window with the specified max window size.  This
	 * constructor does not enable any automatic recurring tasks from being
	 * executed (such as expiration of requests).
	 *
	 * @param size The maximum number of requests permitted to
	 *             be outstanding (unacknowledged) at a given time.  Must be > 0.
	 */
	public Window(int size) {
		this(size, null, 0, null, null);
	}

	/**
	 * Creates a new window with the specified max window size.  This
	 * constructor enables automatic recurring tasks to be executed (such as
	 * expiration of requests).
	 *
	 * @param size            The maximum number of requests permitted to
	 *                        be outstanding (unacknowledged) at a given time.  Must be > 0.
	 * @param executor        The scheduled executor service to execute
	 *                        recurring tasks (such as expiration of requests).
	 * @param monitorInterval The number of milliseconds between executions of
	 *                        monitoring tasks.
	 * @param listener        A listener to send window events to
	 */
	public Window(int size, ScheduledExecutorService executor, long monitorInterval, WindowListener<TKey, TRequest, TResponse> listener) {
		this(size, executor, monitorInterval, listener, null);
	}

	/**
	 * Creates a new window with the specified max window size.  This
	 * constructor enables automatic recurring tasks to be executed (such as
	 * expiration of requests).
	 *
	 * @param size              The maximum number of requests permitted to
	 *                          be outstanding (unacknowledged) at a given time.  Must be > 0.
	 * @param executor          The scheduled executor service to execute
	 *                          recurring tasks (such as expiration of requests).
	 * @param monitorInterval   The number of milliseconds between executions of
	 *                          monitoring tasks.
	 * @param listener          A listener to send window events to
	 * @param monitorThreadName The thread name we'll change to when a monitor
	 *                          run is executed.  Null if no name change is required.
	 */
	public Window(int size, ScheduledExecutorService executor, long monitorInterval, WindowListener<TKey, TRequest, TResponse> listener, String monitorThreadName) {
		if (size <= 0) {
			throw new IllegalArgumentException("size must be > 0");
		}
		this.maxSize = size;
		this.futures = new ConcurrentHashMap<>(size * 2);
		this.lock = new ReentrantLock();
		this.completedCondition = lock.newCondition();
		this.pendingOffers = new AtomicInteger(0);
		this.pendingOffersAborted = new AtomicBoolean(false);
		this.executor = executor;
		this.monitorInterval = monitorInterval;
		this.listeners = new CopyOnWriteArrayList<>();
		if (listener != null) {
			listeners.add(new UnwrappedWeakReference<>(listener));
		}
		if (executor != null) {
			this.monitor = new WindowMonitor<>(this, monitorThreadName);
			this.monitorHandle = executor.scheduleWithFixedDelay(monitor, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
		} else {
			this.monitor = null;
			this.monitorHandle = null;
		}
	}

	/**
	 * Gets the max size of the window.  This is the max number of requests that
	 * can be outstanding (unresponded to) in this window.
	 *
	 * @return The max size of the window
	 */
	public int getMaxSize() {
		return this.maxSize;
	}

	/**
	 * Gets the current number of requests in the window.
	 *
	 * @return The current number of pending requests
	 */
	public int getSize() {
		return futures.size();
	}

	/**
	 * Gets the current number of request that would be accepted by this
	 * window without blocking.  In order words, the number of free slots.
	 *
	 * @return The free size of this window
	 */
	public int getFreeSize() {
		return maxSize - futures.size();
	}

	/**
	 * Returns true if and only if a future with this key exists in this window.
	 *
	 * @param key The key for the future
	 * @return True if the request exists, otherwise false.
	 */
	public boolean containsKey(TKey key) {
		return futures.containsKey(key);
	}

	/**
	 * Gets the a future by its key.
	 *
	 * @param key The key for the request
	 * @return The future or null if it doesn't exist.
	 */
	public WindowFuture<TKey, TRequest, TResponse> get(TKey key) {
		return futures.get(key);
	}

	/**
	 * Adds a new WindowListener if and only if it isn't already present.
	 *
	 * @param listener The listener to add
	 */
	public void addListener(WindowListener<TKey, TRequest, TResponse> listener) {
		listeners.addIfAbsent(new UnwrappedWeakReference<>(listener));
	}

	/**
	 * Removes a WindowListener if it is present.
	 *
	 * @param listener The listener to remove
	 */
	public void removeListener(WindowListener<TKey, TRequest, TResponse> listener) {
		listeners.remove(new UnwrappedWeakReference<>(listener));
	}

	/**
	 * Gets a list of all listeners.
	 *
	 * @return A list of all listeners
	 */
	List<UnwrappedWeakReference<WindowListener<TKey, TRequest, TResponse>>> getListeners() {
		return listeners;
	}

	/**
	 * Destroy this window by freeing all resources associated with it.  All
	 * pending offers are cancelled, followed by all outstanding futures,
	 * then all listeners are removed, and monitoring is cancelled.
	 */
	public synchronized void destroy() {
		try {
			abortPendingOffers();
		} catch (Exception ignored) {
		}
		cancelAll();
		listeners.clear();
		stopMonitor();
	}

	/**
	 * Starts the monitor if this Window has an executor.  Safe to call multiple
	 * times.
	 *
	 * @return True if the monitor was started (true will be returned if it
	 * was already previously started).
	 */
	public synchronized boolean startMonitor() {
		if (executor != null) {
			if (monitorHandle == null) {
				monitorHandle = executor.scheduleWithFixedDelay(monitor, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
			}
			return true;
		}
		return false;
	}

	/**
	 * Stops the monitor if its running.  Safe to call multiple times.
	 */
	public synchronized void stopMonitor() {
		if (monitorHandle != null) {
			monitorHandle.cancel(true);
			monitorHandle = null;
		}
	}

	/**
	 * Creates an ordered snapshot of the requests in this window.  The entries
	 * will be sorted by the natural ascending order of the key.  A new map
	 * is allocated when calling this method, so be careful about calling it
	 * once.
	 *
	 * @return A new map instance representing all requests sorted by
	 * the natural ascending order of its key.
	 */
	public Map<TKey, WindowFuture<TKey, TRequest, TResponse>> createSortedSnapshot() {
		return new TreeMap<>(futures);
	}

	/**
	 * Offers a request for acceptance, waiting for the specified amount of time
	 * in case it could not immediately accepted. The "caller state hint" of
	 * the returned future will be set to "NOT_WAITING". The expireTimestamp of
	 * the returned future will be set to -1 (infinity/never expires).
	 *
	 * @param key                The key for the request. A protocol's sequence number is a
	 *                           good choice.
	 * @param request            The request to offer
	 * @param offerTimeoutMillis The amount of time (in milliseconds) to wait
	 *                           for the offer to be accepted.
	 * @return A future representing pending completion of the request
	 * @throws DuplicateKeyException        Thrown if the key already exists
	 * @throws PendingOfferAbortedException Thrown if the offer could not be
	 *                                      immediately accepted and the caller/thread was waiting, but
	 *                                      the abortPendingOffers() method was called in the meantime.
	 * @throws OfferTimeoutException        Thrown if the offer could not be accepted
	 *                                      within the specified amount of time.
	 * @throws InterruptedException         Thrown if the calling thread is interrupted
	 *                                      while waiting to acquire the internal lock.
	 */
	public WindowFuture offer(TKey key, TRequest request, long offerTimeoutMillis) throws DuplicateKeyException, OfferTimeoutException, InterruptedException {
		return offer(key, request, offerTimeoutMillis, -1, false);
	}

	/**
	 * Offers a request for acceptance, waiting for the specified amount of time
	 * in case it could not immediately accepted. The "caller state hint" of
	 * the returned future will be set to "NOT_WAITING".
	 *
	 * @param key                 The key for the request. A protocol's sequence number is a
	 *                            good choice.
	 * @param request             The request to offer
	 * @param offerTimeoutMillis  The amount of time (in milliseconds) to wait
	 *                            for the offer to be accepted.
	 * @param expireTimeoutMillis The amount of time (in milliseconds) that a
	 *                            request will be set to expire after acceptance.  A value &lt; 1 is
	 *                            assumed to be an infinite expiration (request never expires).
	 *                            Requests are not automatically expired unless monitoring was enabled
	 *                            during construction of this window.
	 * @return A future representing pending completion of the request
	 * @throws DuplicateKeyException        Thrown if the key already exists
	 * @throws PendingOfferAbortedException Thrown if the offer could not be
	 *                                      immediately accepted and the caller/thread was waiting, but
	 *                                      the abortPendingOffers() method was called in the meantime.
	 * @throws OfferTimeoutException        Thrown if the offer could not be accepted
	 *                                      within the specified amount of time.
	 * @throws InterruptedException         Thrown if the calling thread is interrupted
	 *                                      while waiting to acquire the internal lock.
	 */
	public WindowFuture offer(TKey key, TRequest request, long offerTimeoutMillis, long expireTimeoutMillis) throws DuplicateKeyException, OfferTimeoutException, InterruptedException {
		return offer(key, request, offerTimeoutMillis, expireTimeoutMillis, false);
	}

	/**
	 * Offers a request for acceptance, waiting for the specified amount of time
	 * in case it could not immediately accepted.
	 *
	 * @param key                 The key for the request. A protocol's sequence number is a
	 *                            good choice.
	 * @param request             The request to offer
	 * @param offerTimeoutMillis  The amount of time (in milliseconds) to wait
	 *                            for the offer to be accepted.
	 * @param expireTimeoutMillis The amount of time (in milliseconds) that a
	 *                            request will be set to expire after acceptance.  A value &lt; 1 is
	 *                            assumed to be an infinite expiration (request never expires).
	 *                            Requests are not automatically expired unless monitoring was enabled
	 *                            during construction of this window.
	 * @param callerWaitingHint   If true the "caller state hint" of the
	 *                            future will be set to "WAITING" during construction.  This generally
	 *                            does not affect any internal processing by this window, but allows
	 *                            callers to hint they plan on calling "await()" on the future.
	 * @return A future representing pending completion of the request
	 * @throws DuplicateKeyException        Thrown if the key already exists
	 * @throws PendingOfferAbortedException Thrown if the offer could not be
	 *                                      immediately accepted and the caller/thread was waiting, but
	 *                                      the abortPendingOffers() method was called in the meantime.
	 * @throws OfferTimeoutException        Thrown if the offer could not be accepted
	 *                                      within the specified amount of time.
	 * @throws InterruptedException         Thrown if the calling thread is interrupted
	 *                                      while waiting to acquire the internal lock.
	 */
	public WindowFuture offer(TKey key, TRequest request, long offerTimeoutMillis, long expireTimeoutMillis, boolean callerWaitingHint) throws DuplicateKeyException, OfferTimeoutException, PendingOfferAbortedException, InterruptedException {
		if (offerTimeoutMillis < 0) {
			throw new IllegalArgumentException("offerTimeoutMillis must be >= 0 [actual=" + offerTimeoutMillis + "]");
		}

		// does this key already exist?
		if (futures.containsKey(key)) {
			throw new DuplicateKeyException("The key [" + key + "] already exists in the window");
		}

		long offerTimestamp = System.currentTimeMillis();

		lock.lockInterruptibly();
		try {
			// does enough room exist in the "window" for another pending request?
			// NOTE: wait for room up to the offerTimeoutMillis
			// NOTE: multiple signals may be received that will need to be ignored
			while (getFreeSize() <= 0) {
				// check if there time remaining to wait
				long currentOfferTime = System.currentTimeMillis() - offerTimestamp;
				if (currentOfferTime >= offerTimeoutMillis) {
					throw new OfferTimeoutException("Unable to accept offer within [" + offerTimeoutMillis + " ms] (window full)");
				}

				// check if slow waiting was canceled (terminate early)
				if (pendingOffersAborted.get()) {
					throw new PendingOfferAbortedException("Pending offer aborted (by an explicit call to abortPendingOffers())");
				}

				// calculate the amount of timeout remaining
				long remainingOfferTime = offerTimeoutMillis - currentOfferTime;
				try {
					// await for a new signal for this max amount of time
					beginPendingOffer();
					completedCondition.await(remainingOfferTime, TimeUnit.MILLISECONDS);
				} finally {
					boolean abortPendingOffer = endPendingOffer();
					if (abortPendingOffer) {
						throw new PendingOfferAbortedException("Pending offer aborted (by an explicit call to abortPendingOffers())");
					}
				}
			}

			long                                           acceptTimestamp = System.currentTimeMillis();
			long                                           expireTimestamp = (expireTimeoutMillis > 0 ? (acceptTimestamp + expireTimeoutMillis) : -1);
			int                                            callerStateHint = (callerWaitingHint ? WindowFuture.CALLER_WAITING : WindowFuture.CALLER_NOT_WAITING);
			DefaultWindowFuture<TKey, TRequest, TResponse> future          = new DefaultWindowFuture<>(this, lock, completedCondition, key, request, callerStateHint, offerTimeoutMillis, (futures.size() + 1), offerTimestamp, acceptTimestamp, expireTimestamp);
			futures.put(key, future);
			return future;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets the current number of callers/threads that are waiting for a pending
	 * offer to be accepted.
	 */
	public int getPendingOfferCount() {
		return pendingOffers.get();
	}

	/*
	 * Begin waiting for a pending offer to be accepted.  Increments pendingOffers by 1.
	 */
	private void beginPendingOffer() {
		pendingOffers.incrementAndGet();
	}

	/**
	 * End waiting for a pending offer to be accepted.  Decrements pendingOffers by 1.
	 * If "pendingOffersAborted" is true and pendingOffers reaches 0 then
	 * pendingOffersAborted will be reset to false.
	 *
	 * @return True if a pending offer should be aborted. False if a pending
	 * offer can continue waiting if needed.
	 */
	private boolean endPendingOffer() {
		int newValue = pendingOffers.decrementAndGet();
		// if newValue reaches zero, make sure to always reset "offeringAborted"
		if (newValue == 0) {
			// if slotWaitingCanceled was true, then reset it back to false, and
			// return true to make sure the caller knows to cancel waiting
			return pendingOffersAborted.compareAndSet(true, false);
		} else {
			// if slotWaitingCanceled is true, then return true
			return pendingOffersAborted.get();
		}
	}

	/**
	 * Aborts all current callers/threads waiting for a pending offer to be
	 * accepted by the window.
	 *
	 * @return True if there were threads/callers that have a pending offer.
	 * @throws InterruptedException Thrown if the calling thread was interrupted
	 *                              while waiting to obtain the window lock.
	 */
	public boolean abortPendingOffers() throws InterruptedException {
		lock.lockInterruptibly();
		try {
			if (pendingOffers.get() > 0) {
				pendingOffersAborted.set(true);
				completedCondition.signalAll();
				return true;
			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Successfully completes a request by setting the response on the associated future.
	 * Any callers/threads waiting for completion will be signaled. Also, since
	 * this frees up a slot in the window, one caller/thread blocked with a
	 * pending offer will be signaled to continue.
	 *
	 * @param key      The key for the original request
	 * @param response The response to set on the associated future. Null
	 *                 responses are not accepted (use cancel()) instead.
	 * @return A future representing the entire operation. Since a response is
	 * set, the future.isSuccess() method will be true.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public WindowFuture<TKey, TRequest, TResponse> complete(TKey key, TResponse response) throws InterruptedException {
		if (response == null) {
			throw new IllegalArgumentException("Null responses are illegal. Use cancel() instead.");
		}

		if (!futures.containsKey(key)) {
			return null;
		}

		lock.lockInterruptibly();
		try {
			// try to remove future from window
			DefaultWindowFuture<TKey, TRequest, TResponse> future = futures.remove(key);
			if (future == null) {
				return null;
			}

			// set success using helper method (bypasses signalAll and requests.remove(key))
			future.completeHelper(response, System.currentTimeMillis());

			// signal that a future is completed
			completedCondition.signalAll();

			return future;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Fails (completes) a request by setting the cause of the failure on the associated future.
	 * Any callers/threads waiting for completion will be signaled. Also, since
	 * this frees up a slot in the window, one caller/thread blocked with a
	 * pending offer will be signaled to continue.
	 *
	 * @param key   The key for the original request
	 * @param cause The throwable to set as the failure cause on the associated future.
	 *              Null values are not accepted (use cancel()) instead.
	 * @return A future representing the entire operation. Since a cause is
	 * set, the future.isSuccess() method will be false.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public WindowFuture<TKey, TRequest, TResponse> fail(TKey key, Throwable cause) throws InterruptedException {
		if (cause == null) {
			throw new IllegalArgumentException("Null throwables are illegal. Use cancel() instead.");
		}

		if (!futures.containsKey(key)) {
			return null;
		}

		lock.lockInterruptibly();
		try {
			// try to remove future from window
			DefaultWindowFuture<TKey, TRequest, TResponse> future = futures.remove(key);
			if (future == null) {
				return null;
			}

			// set failed using helper method (bypasses signalAll and requests.remove(key))
			future.failedHelper(cause, System.currentTimeMillis());

			// signal that a future is completed
			completedCondition.signalAll();

			return future;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Fails (completes) all requests by setting the same cause of the failure
	 * on all associated futures. Any callers/threads waiting for completion
	 * will be signaled. Also, since this frees up all slots in the window, all
	 * callers/threads blocked with pending offers will be signaled to continue.
	 *
	 * @param cause The throwable to set as the failure cause on all associated futures.
	 *              Null values are not accepted (use cancelAll()) instead.
	 * @return A list of all futures that were failed.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public List<WindowFuture<TKey, TRequest, TResponse>> failAll(Throwable cause) throws InterruptedException {
		if (futures.size() <= 0) {
			return null;
		}

		List<WindowFuture<TKey, TRequest, TResponse>> failed = new ArrayList<>();
		long                                          now    = System.currentTimeMillis();
		lock.lock();
		try {
			// check every request this window contains and see if it's expired
			for (DefaultWindowFuture<TKey, TRequest, TResponse> future : futures.values()) {
				failed.add(future);
				future.failedHelper(cause, now);
			}

			if (failed.size() > 0) {
				futures.clear();
				// signal that a future is completed
				completedCondition.signalAll();
			}
		} finally {
			lock.unlock();
		}
		return failed;
	}

	/**
	 * Cancels (completes) a request. Any callers/threads waiting for completion
	 * will be signaled. Also, since this frees up a slot in the window, one
	 * caller/thread blocked with a pending offer will be signaled to continue.
	 *
	 * @param key The key for the original request
	 * @return A future representing the entire operation.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public WindowFuture<TKey, TRequest, TResponse> cancel(TKey key) throws InterruptedException {
		if (!futures.containsKey(key)) {
			return null;
		}

		lock.lockInterruptibly();
		try {
			// try to remove future from window
			DefaultWindowFuture<TKey, TRequest, TResponse> future = futures.remove(key);
			if (future == null) {
				return null;
			}

			// set failed using helper method (bypasses signalAll and requests.remove(key))
			future.cancelHelper(System.currentTimeMillis());

			// signal that a future is completed
			completedCondition.signalAll();

			return future;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Cancels (completes) all requests. Any callers/threads waiting for completion
	 * will be signaled. Also, since this frees up all slots in the window, all
	 * callers/threads blocked with pending offers will be signaled to continue.
	 *
	 * @return A list of all futures that were cancelled.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public List<WindowFuture<TKey, TRequest, TResponse>> cancelAll() {
		if (futures.size() <= 0) {
			return null;
		}

		List<WindowFuture<TKey, TRequest, TResponse>> cancelled = new ArrayList<>();
		long                                          now       = System.currentTimeMillis();
		lock.lock();
		try {
			// check every request this window contains and see if it's expired
			for (DefaultWindowFuture<TKey, TRequest, TResponse> future : futures.values()) {
				cancelled.add(future);
				future.cancelHelper(now);
			}

			if (cancelled.size() > 0) {
				futures.clear();
				// signal that a future is completed
				completedCondition.signalAll();
			}
		} finally {
			lock.unlock();
		}
		return cancelled;
	}

	/**
	 * Cancels (completes) all expired requests. A request is considered expired
	 * if it has an expireTimestamp set and the current time is &gt;= the
	 * getExpireTimestamp() value. Any callers/threads waiting for completion
	 * will be signaled. Also, if more than one request was expired then all
	 * callers/threads blocked with pending offers will be signaled to continue.
	 *
	 * @return A list of all expired futures that were cancelled.
	 * @throws InterruptedException Thrown if the calling thread is interrupted
	 *                              and we're currently waiting to acquire the internal "windowLock".
	 */
	public List<WindowFuture<TKey, TRequest, TResponse>> cancelAllExpired() {
		if (futures.size() <= 0) {
			return null;
		}

		List<WindowFuture<TKey, TRequest, TResponse>> expired = new ArrayList<>();
		long                                          now     = System.currentTimeMillis();
		lock.lock();
		try {
			// check every request this window contains and see if it's expired
			for (DefaultWindowFuture<TKey, TRequest, TResponse> future : futures.values()) {
				if (future.hasExpireTimestamp() && now >= future.getExpireTimestamp()) {
					expired.add(future);
					future.cancelHelper(now);
				}
			}

			if (expired.size() > 0) {
				// take all expired requests and remove them from the pendingRequests
				for (WindowFuture<TKey, TRequest, TResponse> future : expired) {
					futures.remove(future.getKey());
				}
				// signal that a future is completed
				completedCondition.signalAll();
			}
		} finally {
			lock.unlock();
		}
		return expired;
	}

	void removeHelper(TKey key) {
		futures.remove(key);
	}
}
