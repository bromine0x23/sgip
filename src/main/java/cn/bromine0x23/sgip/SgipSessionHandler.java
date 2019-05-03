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
