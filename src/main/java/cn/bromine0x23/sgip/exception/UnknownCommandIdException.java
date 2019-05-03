package cn.bromine0x23.sgip.exception;

import cn.bromine0x23.sgip.pdu.SgipPdu;

/**
 * 未知指令ID异常
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class UnknownCommandIdException extends RecoverablePduException {

	private static final long serialVersionUID = -3065460447181122908L;

	public UnknownCommandIdException(SgipPdu pdu, String message) {
		super(pdu, message);
	}

	public UnknownCommandIdException(SgipPdu pdu, String message, Throwable throwable) {
		super(pdu, message, throwable);
	}
}
