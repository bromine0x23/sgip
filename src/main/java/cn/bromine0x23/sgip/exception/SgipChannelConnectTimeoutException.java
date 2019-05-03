package cn.bromine0x23.sgip.exception;

/**
 * Thrown for exceptions occuring while attempting to connect to a remote
 * system and cannot complete within a period of time.
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipChannelConnectTimeoutException extends SgipChannelConnectException {

	private static final long serialVersionUID = 1048359885300244803L;

	public SgipChannelConnectTimeoutException(String message) {
		super(message);
	}

	public SgipChannelConnectTimeoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
