/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.SgipChannelException;
import cn.bromine0x23.sgip.exception.SgipTimeoutException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.pdu.SgipPduRequest;
import cn.bromine0x23.sgip.pdu.SgipPduResponse;
import cn.bromine0x23.sgip.pdu.SgipSubmit;
import cn.bromine0x23.sgip.pdu.SgipSubmitResp;
import cn.bromine0x23.sgip.windowing.WindowFuture;

/**
 * SGIP会话接口
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipSession {

	enum State {
		INITIAL, OPEN, BINDING, BOUND, UNBINDING, CLOSED
	}

	boolean isOpen();

	boolean isBinding();

	boolean isBound();

	boolean isUnbinding();

	boolean isClosed();

	SgipSubmitResp submit(
		SgipSubmit request, long timeoutMillis
	) throws RecoverablePduException, UnrecoverablePduException, SgipTimeoutException, SgipChannelException, InterruptedException;

	void unbind(long timeoutMillis);

	void close();

	void close(long timeoutMillis);

	void destroy();

	WindowFuture<Integer, SgipPduRequest, SgipPduResponse> sendRequestPdu(
		SgipPduRequest request, long timeoutMillis, boolean synchronous
	) throws RecoverablePduException, UnrecoverablePduException, SgipTimeoutException, SgipChannelException, InterruptedException;

	void sendResponsePdu(
		SgipPduResponse response
	) throws RecoverablePduException, UnrecoverablePduException, SgipChannelException, InterruptedException;
}
