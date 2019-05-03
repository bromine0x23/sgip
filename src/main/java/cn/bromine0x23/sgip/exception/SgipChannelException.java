package cn.bromine0x23.sgip.exception;

/**
 * Thrown for exceptions occurring with the underlying Channel or TCP/IP
 * connection such as read/write errors, closed connections, etc.
 *
 * Its best to restart a session if this occurs.
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipChannelException extends Exception {

	private static final long serialVersionUID = 3752978918573150222L;

	public SgipChannelException(String message) {
		super(message);
	}

	public SgipChannelException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
