/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.pdu.SgipPduRequest;
import cn.bromine0x23.sgip.pdu.SgipPduResponse;

/**
 * SGIP会话处理器接口
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipSessionHandler {

	void channelUnexpectedlyClosed();

	SgipPduResponse pduRequestReceived(SgipPduRequest request);

	void pduRequestExpired(SgipPduRequest request);

	void expectedPduResponseReceived(SgipAsyncPduResponse response);

	void unexpectedPduResponseReceived(SgipPduResponse response);

	void unrecoverablePduException(UnrecoverablePduException exception);

	void recoverablePduException(RecoverablePduException exception);

	void unknownThrowable(Throwable throwable);
}
