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

import lombok.Getter;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of a WindowFuture.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class DefaultWindowFuture<TKey, TRequest, TResponse> implements WindowFuture<TKey, TRequest, TResponse> {

	private final WeakReference<Window<TKey, TRequest, TResponse>>      window;
	private final ReentrantLock              windowLock;
	private final Condition                  completedCondition;
	@Getter
	private final TKey                       key;
	@Getter
	private final TRequest                   request;
	private final AtomicReference<TResponse> response;
	private final AtomicReference<Throwable> cause;
	private final AtomicInteger              callerStateHint;
	private final AtomicBoolean              done;
	private final long                       originalOfferTimeoutMillis;
	private final int                        windowSize;
	@Getter
	private final long                       offerTimestamp;
	@Getter
	private final long                       acceptTimestamp;
	@Getter
	private final long                       expireTimestamp;
	private final AtomicLong                 doneTimestamp;

	/**
	 * Creates a new DefaultWindowFuture.
	 *
	 * @param window                     The window that created this future.  Saved as a weak
	 *                                   reference to prevent circular references.
	 * @param windowLock                 The shared lock from the window
	 * @param completedCondition         The shared condition to wait on
	 * @param key                        The key of the future
	 * @param request                    The request of the future
	 * @param callerStateHint            The initial state of the caller hint
	 * @param originalOfferTimeoutMillis
	 * @param windowSize                 Size of the window after this request was added. Useful
	 *                                   for calculating an estimated response time for this request rather
	 *                                   than all requests ahead of it in the window.
	 * @param offerTimestamp             The timestamp when the request was offered
	 * @param acceptTimestamp            The timestamp when the request was accepted
	 * @param expireTimestamp            The timestamp when the request will expire or -1
	 *                                   if no expiration is set
	 */
	protected DefaultWindowFuture(Window<TKey, TRequest, TResponse> window, ReentrantLock windowLock, Condition completedCondition, TKey key, TRequest request, int callerStateHint, long originalOfferTimeoutMillis, int windowSize, long offerTimestamp, long acceptTimestamp, long expireTimestamp) {
		this.window = new WeakReference<>(window);
		this.windowLock = windowLock;
		this.completedCondition = completedCondition;
		this.key = key;
		this.request = request;
		this.response = new AtomicReference<>();
		this.cause = new AtomicReference<>();
		this.callerStateHint = new AtomicInteger(callerStateHint);
		this.done = new AtomicBoolean(false);
		this.originalOfferTimeoutMillis = originalOfferTimeoutMillis;
		this.windowSize = windowSize;
		this.offerTimestamp = offerTimestamp;
		this.acceptTimestamp = acceptTimestamp;
		this.expireTimestamp = expireTimestamp;
		this.doneTimestamp = new AtomicLong(0);
	}

	@Override
	public TResponse getResponse() {
		return response.get();
	}

	@Override
	public Throwable getCause() {
		return cause.get();
	}

	@Override
	public int getCallerStateHint() {
		return callerStateHint.get();
	}

	public void setCallerStateHint(int callerState) {
		callerStateHint.set(callerState);
	}

	@Override
	public boolean isCallerWaiting() {
		return (callerStateHint.get() == CALLER_WAITING);
	}

	@Override
	public int getWindowSize() {
		return windowSize;
	}

	@Override
	public boolean hasExpireTimestamp() {
		return expireTimestamp > 0;
	}

	@Override
	public boolean hasDoneTimestamp() {
		return doneTimestamp.get() > 0;
	}

	@Override
	public long getDoneTimestamp() {
		return doneTimestamp.get();
	}

	@Override
	public long getOfferToAcceptTime() {
		return acceptTimestamp - offerTimestamp;
	}

	@Override
	public long getOfferToDoneTime() {
		if (done.get()) {
			return (doneTimestamp.get() - offerTimestamp);
		} else {
			return -1;
		}
	}

	@Override
	public long getAcceptToDoneTime() {
		if (done.get()) {
			return (doneTimestamp.get() - acceptTimestamp);
		} else {
			return -1;
		}
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	private void lockAndSignalAll() {
		// notify any waiters that we're done
		windowLock.lock();
		try {
			completedCondition.signalAll();
		} finally {
			windowLock.unlock();
		}
	}

	@Override
	public boolean isSuccess() {
		return (done.get() && response.get() != null);
	}

	@Override
	public void complete(TResponse response) {
		complete(response, System.currentTimeMillis());
	}

	@Override
	public void complete(TResponse response, long doneTimestamp) {
		completeHelper(response, doneTimestamp);
		safelyRemoveRequestInWindow();
		lockAndSignalAll();
	}

	private void safelyRemoveRequestInWindow() {
		Window<TKey, TRequest, TResponse> window0 = window.get();
		if (window0 == null) {
			// hmm.. this means the window was garbage collected (uh oh)
		} else {
			window0.removeHelper(key);
		}
	}

	void completeHelper(TResponse response, long doneTimestamp) {
		if (response == null) {
			throw new IllegalArgumentException("A response cannot be null if trying to complete()");
		}
		if (doneTimestamp <= 0) {
			throw new IllegalArgumentException("A valid doneTime must be > 0 if trying to complete()");
		}
		// set to done, but don't handle duplicate calls
		if (!this.done.get()) {
			this.response.set(response);
			this.doneTimestamp.set(doneTimestamp);
			this.done.set(true);
		}
	}

	@Override
	public void fail(Throwable t) {
		fail(t, System.currentTimeMillis());
	}

	@Override
	public void fail(Throwable t, long doneTimestamp) {
		failedHelper(t, doneTimestamp);
		safelyRemoveRequestInWindow();
		lockAndSignalAll();
	}

	void failedHelper(Throwable t, long doneTimestamp) {
		if (t == null) {
			throw new IllegalArgumentException("A response cannot be null if trying to failed()");
		}
		if (doneTimestamp <= 0) {
			throw new IllegalArgumentException("A valid doneTimestamp must be > 0 if trying to failed()");
		}
		// set to done, but don't handle duplicate calls
		if (!this.done.get()) {
			this.cause.set(t);
			this.doneTimestamp.set(doneTimestamp);
			this.done.set(true);
		}
	}

	@Override
	public boolean isCancelled() {
		return (this.done.get() && this.response.get() == null && this.cause.get() == null);
	}

	@Override
	public void cancel() {
		cancel(System.currentTimeMillis());
	}

	@Override
	public void cancel(long doneTimestamp) {
		cancelHelper(doneTimestamp);
		safelyRemoveRequestInWindow();
		lockAndSignalAll();
	}

	void cancelHelper(long doneTimestamp) {
		if (doneTimestamp <= 0) {
			throw new IllegalArgumentException("A valid doneTimestamp must be > 0 if trying to cancel()");
		}
		// set to done, but don't handle duplicate calls
		if (this.done.compareAndSet(false, true)) {
			this.doneTimestamp.set(doneTimestamp);
		}
	}

	@Override
	public boolean await() throws InterruptedException {
		// wait for only offerTimeoutMillis - offerToAcceptTime
		long remainingTimeoutMillis = this.originalOfferTimeoutMillis - this.getOfferToAcceptTime();
		return this.await(remainingTimeoutMillis);

	}

	@Override
	public boolean await(long timeoutMillis) throws InterruptedException {
		// k, if someone actually calls this method -- make sure to set the flag
		// this may have already been set earlier, but if not its safe to set here
		this.setCallerStateHint(CALLER_WAITING);

		// if already done, return immediately
		if (isDone()) {
			return true;
		}

		long startTime = System.currentTimeMillis();
		// try to acquire lock within given amount of time
		if (!windowLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
			this.setCallerStateHint(CALLER_WAITING_TIMEOUT);
			return false;
		}

		try {
			// keep waiting until we're done
			while (!isDone()) {
				// current "waitTime" is ("now" - startTime)
				long waitingTime = System.currentTimeMillis() - startTime;
				if (waitingTime >= timeoutMillis) {
					// caller intended on waiting, but timed out while waiting for a response
					this.setCallerStateHint(CALLER_WAITING_TIMEOUT);
					return false;
				}
				// calculate the amount of timeout remaining
				long remainingWaitTime = timeoutMillis - waitingTime;
				// await for a signal that a response was received
				// NOTE: this signal may be sent multiple times and not apply to us necessarily
				completedCondition.await(remainingWaitTime, TimeUnit.MILLISECONDS);
			}
		} finally {
			windowLock.unlock();
		}

		return true;
	}
}
