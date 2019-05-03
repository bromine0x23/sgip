package cn.bromine0x23.sgip.exception;

/**
 * 不可恢复的PDU异常
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class UnrecoverablePduException extends Exception {

	private static final long serialVersionUID = 8849757357109053172L;

	public UnrecoverablePduException(String message) {
		super(message);
	}

	public UnrecoverablePduException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
