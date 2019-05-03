package cn.bromine0x23.sgip.exception;

import cn.bromine0x23.sgip.pdu.SgipPdu;
import lombok.Getter;
import lombok.Setter;

/**
 * 可恢复的PDU异常
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class RecoverablePduException extends Exception {

	private static final long serialVersionUID = 9136098376704801699L;

	@Getter
	@Setter
	private SgipPdu partialPdu;

	public RecoverablePduException(String message) {
		super(message);
	}

	public RecoverablePduException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public RecoverablePduException(SgipPdu partialPdu, String message) {
		super(message);
		this.partialPdu = partialPdu;
	}

	public RecoverablePduException(SgipPdu partialPdu, String message, Throwable throwable) {
		super(message, throwable);
		this.partialPdu = partialPdu;
	}
}
