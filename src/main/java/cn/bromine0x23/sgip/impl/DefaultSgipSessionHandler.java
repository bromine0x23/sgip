package cn.bromine0x23.sgip.impl;

import cn.bromine0x23.sgip.SgipAsyncPduResponse;
import cn.bromine0x23.sgip.SgipSessionListener;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.pdu.SgipPdu;
import cn.bromine0x23.sgip.pdu.SgipPduRequest;
import cn.bromine0x23.sgip.pdu.SgipPduResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.ClosedChannelException;

/**
 * SGIP会话处理器实现
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class DefaultSgipSessionHandler implements SgipSessionListener {

	private final Logger logger;

	public DefaultSgipSessionHandler() {
		this(LoggerFactory.getLogger(DefaultSgipSessionHandler.class));
	}

	public DefaultSgipSessionHandler(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void channelUnexpectedlyClosed() {
		logger.info("Default handling is to discard an unexpected channel closed");
	}

	@Override
	public SgipPduResponse pduRequestReceived(SgipPduRequest request) {
		logger.warn("Default handling is to discard unexpected request PDU: {}", request);
		return null;
	}

	@Override
	public void pduRequestExpired(SgipPduRequest request) {
		logger.warn("Default handling is to discard expired request PDU: {}", request);
	}

	@Override
	public void expectedPduResponseReceived(SgipAsyncPduResponse response) {
		logger.warn("Default handling is to discard unexpected response PDU: {}", response);
	}

	@Override
	public void unexpectedPduResponseReceived(SgipPduResponse response) {
		logger.warn("Default handling is to discard unexpected response PDU: {}", response);
	}

	@Override
	public void unrecoverablePduException(UnrecoverablePduException exception) {
		logger.warn("Default handling is to discard a unrecoverable exception:", exception);
	}

	@Override
	public void recoverablePduException(RecoverablePduException exception) {
		logger.warn("Default handling is to discard a recoverable exception:", exception);
	}

	@Override
	public void unknownThrowable(Throwable throwable) {
		if (throwable instanceof ClosedChannelException) {
			logger.warn("Unknown throwable received, but it was a ClosedChannelException, calling fireChannelUnexpectedlyClosed instead");
			channelUnexpectedlyClosed();
		} else {
			logger.warn("Default handling is to discard an unknown throwable:", throwable);
		}
	}

	@Override
	public boolean pduReceived(SgipPdu pdu) {
		return true;
	}

	@Override
	public boolean pduDispatch(SgipPdu pdu) {
		return true;
	}
}
