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

/**
 * A {@link WindowFuture} is either <em>uncompleted</em> or <em>completed</em>.
 * When an operation begins, a new future object is created.  The new future
 * is uncompleted initially - it is neither succeeded, failed, nor cancelled
 * because the operation is not finished yet.  If the operation is
 * finished either successfully, with failure, or by cancellation, the future is
 * marked as completed with more specific information, such as the cause of the
 * failure.  Please note that even failure and cancellation belong to the
 * completed state.
 * <pre>
 *                                      +---------------------------+
 *                                      | Completed successfully    |
 *                                      +---------------------------+
 *                                 +---->      isDone() = <b>true</b>      |
 * +--------------------------+    |    |   isSuccess() = <b>true</b>      |
 * |        Uncompleted       |    |    +===========================+
 * +--------------------------+    |    | Completed with failure    |
 * |      isDone() = false    |    |    +---------------------------+
 * |   isSuccess() = false    |----+---->   isDone() = <b>true</b>         |
 * | isCancelled() = false    |    |    | getCause() = <b>non-null</b>     |
 * |    getCause() = null     |    |    +===========================+
 * +--------------------------+    |    | Completed by cancellation |
 *                                 |    +---------------------------+
 *                                 +---->      isDone() = <b>true</b>      |
 *                                      | isCancelled() = <b>true</b>      |
 *                                      +---------------------------+
 * </pre>
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public interface WindowFuture<TKey, TRequest, TResponse> {

	/**
	 * The caller is not waiting on this entry
	 */
	int CALLER_NOT_WAITING = 0;

	/**
	 * The caller is waiting on this entry
	 */
	int CALLER_WAITING = 1;

	/**
	 * The caller was waiting, but gave up (timeout)
	 */
	int CALLER_WAITING_TIMEOUT = 2;

	/**
	 * Gets the key of the window entry.
	 *
	 * @return The key of the window entry.
	 */
	TKey getKey();

	/**
	 * Gets the request contained in the window entry.
	 *
	 * @return The request contained in the window entry.
	 */
	TRequest getRequest();

	/**
	 * Gets the response associated with the window entry.
	 *
	 * @return The response associated with he request or null if no response
	 * has been received.
	 */
	TResponse getResponse();

	/**
	 * Returns {@code true} if and only if this future is
	 * complete, regardless of whether the operation was successful, failed,
	 * or cancelled.
	 */
	boolean isDone();

	/**
	 * Returns {@code true} if and only if the operation was completed
	 * successfully.
	 */
	boolean isSuccess();

	/**
	 * Returns {@code true} if and only if this future was cancelled by a
	 * cancel() method.
	 */
	boolean isCancelled();

	/**
	 * Returns {@code true} if and only if the caller hinted at "WAITING" for
	 * completion.  Returns {@code false} if the caller either did not hint
	 * at planning on "WAITING" or did wait but timed out and gave up. Please
	 * note that even if this returns true, it does not mean the caller is
	 * actively waiting since this merely represents a hint.
	 */
	boolean isCallerWaiting();

	/**
	 * Returns the cause of the failed operation if the operation has
	 * failed.
	 *
	 * @return the cause of the failure.
	 * {@code null} if succeeded or this future is not
	 * completed yet.
	 */
	Throwable getCause();

	/**
	 * Gets a hint of the caller state such as whether the caller is waiting, not
	 * waiting, or timed out while waiting for completion.
	 *
	 * @return The hint of the state of the caller
	 */
	int getCallerStateHint();

	/**
	 * Returns the size of the window (number of requests in it) after this
	 * request was added.  Useful for calculating an estimated response time
	 * just for this request.
	 *
	 * @return The size of the window after this request was added.
	 */
	int getWindowSize();

	/**
	 * Returns true if an expire timestamp value exists (&gt; 0).
	 *
	 * @return True if an expire timestamp exists
	 */
	boolean hasExpireTimestamp();

	/**
	 * Gets the expire timestamp in milliseconds.  The expire timestamp is when
	 * the request expires unless this optional field was not set.
	 *
	 * @return The expire timestamp or &lt;= 0 if it doesn't exist.
	 * @see #hasExpireTimestamp()
	 */
	long getExpireTimestamp();

	/**
	 * Gets the offer timestamp in milliseconds.  The offer timestamp is when
	 * the request was offered for acceptance to the window.
	 *
	 * @return The offer timestamp
	 */
	long getOfferTimestamp();

	/**
	 * Gets the accept timestamp in milliseconds. The accept timestamp is when
	 * the request was accepted by the window.
	 *
	 * @return The accept timestamp
	 */
	long getAcceptTimestamp();

	/**
	 * Gets the amount of time (in ms) from offer to accept.
	 *
	 * @return The amount of time from offer to accept
	 */
	long getOfferToAcceptTime();

	/**
	 * Returns true if a done timestamp value exists (&gt; 0).
	 *
	 * @return True if a done timestamp exists
	 */
	boolean hasDoneTimestamp();

	/**
	 * Gets the done timestamp in milliseconds. The done timestamp is when
	 * the request has been completed.
	 *
	 * @return The done timestamp
	 */
	long getDoneTimestamp();

	/**
	 * Gets the amount of time (in ms) from offer to done or -1 if a done
	 * timestamp does not yet exist.
	 *
	 * @return The amount of time from offer to done
	 */
	long getOfferToDoneTime();

	/**
	 * Gets the amount of time (in ms) from accept to done or -1 if a done
	 * timestamp does not yet exist.
	 *
	 * @return The amount of time from accept to done
	 */
	long getAcceptToDoneTime();

	/**
	 * Completes (as a success) a request by setting the response. This method
	 * will set the done timestamp to System.currentTimeMillis().
	 *
	 * @param response The response for the associated request
	 */
	void complete(TResponse response);

	/**
	 * Completes (as a success) a request by setting the response.
	 *
	 * @param response      The response for the associated request
	 * @param doneTimestamp The timestamp when the request completed
	 */
	void complete(TResponse response, long doneTimestamp);

	/**
	 * Completes (as a failure) a request by setting a throwable as the cause
	 * of failure. This method will set the done timestamp to System.currentTimeMillis()
	 *
	 * @param t The throwable as the cause of failure
	 */
	void fail(Throwable t);

	/**
	 * Completes (as a failure) a request by setting a throwable as the cause
	 * of failure.
	 *
	 * @param t             The throwable as the cause of failure
	 * @param doneTimestamp The timestamp when the request failed
	 */
	void fail(Throwable t, long doneTimestamp);

	/**
	 * Completes (as a cancel) a request. This method will set the done timestamp
	 * to System.currentTimeMillis().
	 */
	void cancel();

	/**
	 * Completes (as a cancel) a request.
	 *
	 * @param doneTimestamp The timestamp when the request was cancelled
	 */
	void cancel(long doneTimestamp);

	/**
	 * Waits for this future to be completed within the amount of time remaining
	 * from the original offerTimeoutMillis minus the amount of time it took
	 * for the Window to accept the offer. For example, if Window.offer was
	 * called with an offerTimeoutMillis of 5000 milliseconds and it took
	 * 1000 milliseconds for the Window to accept the offer, then this method
	 * would wait for 4000 milliseconds.
	 *
	 * @return True if and only if the future was completed within the specified
	 * time limit
	 * @throws InterruptedException Thrown if the current thread was interrupted
	 */
	boolean await() throws InterruptedException;

	/**
	 * Waits for this future to be completed within the specified time limit.
	 *
	 * @param timeoutMillis The amount of milliseconds to wait
	 * @return True if and only if the future was completed within the specified
	 * time limit
	 * @throws InterruptedException Thrown if the current thread was interrupted
	 */
	boolean await(long timeoutMillis) throws InterruptedException;
}