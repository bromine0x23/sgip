/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.impl;

import cn.bromine0x23.sgip.pdu.*;
import cn.bromine0x23.sgip.windowing.*;
import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.SgipSession;
import cn.bromine0x23.sgip.SgipSessionChannelListener;
import cn.bromine0x23.sgip.SgipSessionConfiguration;
import cn.bromine0x23.sgip.SgipSessionHandler;
import cn.bromine0x23.sgip.SgipSessionListener;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.SgipBindException;
import cn.bromine0x23.sgip.exception.SgipChannelException;
import cn.bromine0x23.sgip.exception.SgipTimeoutException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SGIP会话实现
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class DefaultSgipSession implements SgipSession, SgipSessionChannelListener, WindowListener<Integer, SgipPduRequest, SgipPduResponse> {

	private static final Logger logger = LoggerFactory.getLogger(DefaultSgipSession.class);

	private final AtomicReference<State> state = new AtomicReference<>(State.OPEN);

	private final AtomicLong boundTime = new AtomicLong(0);

	private final AtomicInteger sequenceNumber = new AtomicInteger();

	@Getter
	private SgipSessionConfiguration configuration;

	@Getter
	private Channel channel;

	@Getter
	private SgipSessionHandler sessionHandler;

	private final Window<Integer, SgipPduRequest, SgipPduResponse> sendWindow;

	public DefaultSgipSession(
		SgipSessionConfiguration configuration,
		Channel channel,
		SgipSessionHandler sessionHandler,
		ScheduledExecutorService monitorExecutor
	) {
		this.configuration = configuration;
		this.channel = channel;
		this.sessionHandler = sessionHandler;

		if (monitorExecutor != null && configuration.getWindowMonitorInterval() > 0) {
			this.sendWindow = new Window<>(
				configuration.getWindowSize(), monitorExecutor, configuration.getWindowMonitorInterval(), this, null);
		} else {
			this.sendWindow = new Window<>(configuration.getWindowSize());
		}
	}

	@Override
	public boolean isOpen() {
		return state.get() == State.OPEN;
	}

	@Override
	public boolean isBinding() {
		return state.get() == State.BINDING;
	}

	@Override
	public boolean isBound() {
		return state.get() == State.BOUND;
	}

	@Override
	public boolean isUnbinding() {
		return state.get() == State.UNBINDING;
	}

	@Override
	public boolean isClosed() {
		return state.get() == State.CLOSED;
	}

	public SgipBindResp bind(
		SgipBind request,
		long timeoutInMillis
	) throws RecoverablePduException, UnrecoverablePduException, SgipBindException, SgipTimeoutException, SgipChannelException, InterruptedException {
		boolean bound = false;
		try {
			state.set(State.BINDING);
			SgipPduResponse response     = sendRequestAndGetResponse(request, timeoutInMillis);
			SgipBindResp    bindResponse = (SgipBindResp)response;
			if (bindResponse == null || bindResponse.getResult() != SgipConstants.ERROR_CODE_OK) {
				throw new SgipBindException(bindResponse);
			}
			bound = true;
			return bindResponse;
		} finally {
			if (bound) {
				state.set(State.BOUND);
				boundTime.set(System.currentTimeMillis());
			} else {
				try {
					this.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	@Override
	public SgipSubmitResp submit(
		SgipSubmit request,
		long timeoutMillis
	) throws RecoverablePduException, UnrecoverablePduException, SgipTimeoutException, SgipChannelException, InterruptedException {
		SgipPduResponse response = sendRequestAndGetResponse(request, timeoutMillis);
		return (SgipSubmitResp)response;
	}


	@Override
	public void unbind(long timeoutMillis) {
		if (channel.isActive()) {
			state.set(State.UNBINDING);
			// try a "graceful" unbind by sending an "unbind" request
			try {
				sendRequestAndGetResponse(new SgipUnbind(), timeoutMillis);
			} catch (Exception exception) {
				logger.warn("Did not cleanly receive an unbind response to our unbind request, safe to ignore: {}", exception.getMessage());
			}
		} else {
			logger.info("Session channel is already closed, not going to unbind");
		}
		close(timeoutMillis);
	}

	@Override
	public void close() {
		close(5000);
	}

	@Override
	public void close(long timeoutMillis) {
		if (channel.isActive()) {
			state.set(State.UNBINDING);
			if (channel.close().awaitUninterruptibly(timeoutMillis)) {
				logger.info("Successfully closed");
			} else {
				logger.warn("Unable to cleanly close channel");
			}
		}
		this.state.set(State.CLOSED);
	}

	@Override
	public void destroy() {
		close();
		sendWindow.destroy();
		sessionHandler = null;
	}

	@Override
	public WindowFuture<Integer, SgipPduRequest, SgipPduResponse> sendRequestPdu(
		SgipPduRequest request,
		long timeoutMillis,
		boolean synchronous
	) throws UnrecoverablePduException, SgipTimeoutException, SgipChannelException, InterruptedException {
		if (!request.hasSourceNodeIdAssigned()) {
			request.setSourceNodeId(configuration.getSourceNodeId());
		}
		if (!request.hasTimestampAssigned()) {
			request.setTimestamp(SgipPdu.calculateTimestamp());
		}
		if (!request.hasSequenceNumberAssigned()) {
			request.setSequenceNumber(sequenceNumber.getAndIncrement());
		}

		WindowFuture<Integer, SgipPduRequest, SgipPduResponse> windowFuture;
		try {
			//noinspection unchecked
			windowFuture = (WindowFuture<Integer, SgipPduRequest, SgipPduResponse>)sendWindow.offer(
				request.getSequenceNumber(), request, timeoutMillis, configuration.getRequestExpiryTimeout(), synchronous
			);
		} catch (DuplicateKeyException exception) {
			throw new UnrecoverablePduException(exception.getMessage(), exception);
		} catch (OfferTimeoutException exception) {
			throw new SgipTimeoutException(exception.getMessage(), exception);
		}

		if (sessionHandler instanceof SgipSessionListener && !((SgipSessionListener)this.sessionHandler).pduDispatch(request)) {
			logger.info("dispatched request PDU discarded: {}", request);
			windowFuture.cancel();
			return windowFuture;
		}

		if (configuration.isLogPduEnabled()) {
			if (synchronous) {
				logger.info("sync send PDU: {}", request);
			} else {
				logger.info("async send PDU: {}", request);
			}
		}

		ChannelFuture channelFuture = channel.writeAndFlush(request).await();

		if (!channelFuture.isSuccess()) {
			throw new SgipChannelException(channelFuture.cause().getMessage(), channelFuture.cause());
		}

		return windowFuture;
	}

	@Override
	public void sendResponsePdu(SgipPduResponse response) throws SgipChannelException, InterruptedException {
		if (!response.hasSourceNodeIdAssigned()) {
			response.setSourceNodeId(configuration.getSourceNodeId());
		}
		if (!response.hasTimestampAssigned()) {
			response.setTimestamp(SgipPdu.calculateTimestamp());
		}
		if (!response.hasSequenceNumberAssigned()) {
			response.setSequenceNumber(sequenceNumber.getAndIncrement());
		}

		if (sessionHandler instanceof SgipSessionListener && !((SgipSessionListener)this.sessionHandler).pduDispatch(response)) {
			logger.info("dispatched response PDU discarded: {}", response);
			return;
		}

		if (configuration.isLogPduEnabled()) {
			logger.info("send PDU: {}", response);
		}

		ChannelFuture channelFuture = channel.writeAndFlush(response).await();

		if (!channelFuture.isSuccess()) {
			throw new SgipChannelException(channelFuture.cause().getMessage(), channelFuture.cause());
		}
	}

	@Override
	public void pduReceived(SgipPdu pdu) {
		if (configuration.isLogPduEnabled()) {
			logger.info("received PDU: {}", pdu);
		}
		if (sessionHandler instanceof SgipSessionListener && !((SgipSessionListener)this.sessionHandler).pduReceived(pdu)) {
			logger.info("recieved PDU discarded: {}", pdu);
			return;
		}
		if (pdu instanceof SgipPduRequest) {
			pduRequestReceived((SgipPduRequest)pdu);
		} else {
			pduResponseReceived((SgipPduResponse)pdu);
		}
	}

	@Override
	public void exceptionCaught(Throwable throwable) {
		if (throwable instanceof UnrecoverablePduException) {
			sessionHandler.unrecoverablePduException((UnrecoverablePduException)throwable);
		} else if (throwable instanceof RecoverablePduException) {
			sessionHandler.recoverablePduException((RecoverablePduException)throwable);
		} else {
			if (isUnbinding() || isClosed()) {
				logger.debug("Unbind/close was requested, ignoring exception thrown: {}", throwable);
			} else {
				sessionHandler.unknownThrowable(throwable);
			}
		}
	}

	@Override
	public void channelInactive() {
		if (sendWindow.getSize() > 0) {
			logger.trace("Channel closed and sendWindow has [{}] outstanding requests, some may need cancelled immediately", sendWindow.getSize());
			Map<Integer, WindowFuture<Integer, SgipPduRequest, SgipPduResponse>> requests = sendWindow.createSortedSnapshot();
			Throwable                                                            cause    = new ClosedChannelException();
			for (WindowFuture<Integer, SgipPduRequest, SgipPduResponse> future : requests.values()) {
				if (future.isCallerWaiting()) {
					logger.debug("Caller waiting on request [{}], cancelling it with a channel closed exception", future.getKey());
					try {
						future.fail(cause);
					} catch (Exception ignored) {
					}
				}
			}
		}
		if (isUnbinding() || isClosed()) {
			logger.debug("Unbind/close was requested, ignoring channelClosed event");
		} else {
			sessionHandler.channelUnexpectedlyClosed();
		}
	}

	private void pduRequestReceived(SgipPduRequest request) {
		SgipPduResponse responsePdu = sessionHandler.pduRequestReceived(request);
		if (responsePdu != null) {
			try {
				sendResponsePdu(responsePdu);
			} catch (Exception exception) {
				logger.error("Unable to cleanly return response PDU: {}", exception);
			}
		}
	}

	private void pduResponseReceived(SgipPduResponse response) {
		int receivedPduSeqNum = response.getSequenceNumber();
		try {
			WindowFuture<Integer, SgipPduRequest, SgipPduResponse> future = sendWindow.complete(receivedPduSeqNum, response);
			if (future != null) {
				logger.trace("Found a future in the window for seqNum [{}]", receivedPduSeqNum);
				int callerStateHint = future.getCallerStateHint();
				//logger.trace("IsCallerWaiting? " + future.isCallerWaiting() + " callerStateHint=" + callerStateHint);
				if (callerStateHint == WindowFuture.CALLER_WAITING) {
					logger.trace("Caller waiting for request: {}", future.getRequest());
				} else if (callerStateHint == WindowFuture.CALLER_NOT_WAITING) {
					logger.trace("Caller not waiting for request: {}", future.getRequest());
					sessionHandler.expectedPduResponseReceived(new DefaultSgipAsyncPduResponse(future));
				} else {
					logger.trace("Caller timed out waiting for request: {}", future.getRequest());
					sessionHandler.unexpectedPduResponseReceived(response);
				}
			} else {
				sessionHandler.unexpectedPduResponseReceived(response);
			}
		} catch (InterruptedException exception) {
			logger.warn("Interrupted while attempting to process response PDU and match it to a request via requestWindow: ", exception);
		}
	}

	private SgipPduResponse sendRequestAndGetResponse(
		SgipPduRequest requestPdu,
		long timeoutInMillis
	) throws RecoverablePduException, UnrecoverablePduException, SgipTimeoutException, SgipChannelException, InterruptedException {
		WindowFuture<Integer, SgipPduRequest, SgipPduResponse> future                 = sendRequestPdu(requestPdu, timeoutInMillis, true);
		boolean                                                completedWithinTimeout = future.await();
		if (!completedWithinTimeout) {
			future.cancel();
			throw new SgipTimeoutException("Unable to get response within [" + timeoutInMillis + " ms]");
		}
		if (future.isSuccess()) {
			return future.getResponse();
		} else if (future.getCause() != null) {
			Throwable cause = future.getCause();
			if (cause instanceof ClosedChannelException) {
				throw new SgipChannelException("Channel was closed after sending request, but before receiving response", cause);
			} else {
				throw new UnrecoverablePduException(cause.getMessage(), cause);
			}
		} else if (future.isCancelled()) {
			throw new RecoverablePduException("Request was cancelled");
		} else {
			throw new UnrecoverablePduException("Unable to sendRequestAndGetResponse successfully (future was in strange state)");
		}
	}

	@Override
	public void expired(WindowFuture<Integer, SgipPduRequest, SgipPduResponse> windowFuture) {
		sessionHandler.pduRequestExpired(windowFuture.getRequest());
	}
}
