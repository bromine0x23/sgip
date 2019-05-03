package cn.bromine0x23.sgip.exception;

/**
 * Thrown for exceptions occurring with the underlying Channel or TCP/IP
 * connection such as read/write errors, closed connections, etc.
 *
 * Its best to restart a session if this occurs.
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipTimeoutException extends Exception {

	private static final long serialVersionUID = -5774306850074231384L;

	public SgipTimeoutException(String message) {
		super(message);
	}

	public SgipTimeoutException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
