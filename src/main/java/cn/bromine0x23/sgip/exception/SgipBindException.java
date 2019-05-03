package cn.bromine0x23.sgip.exception;

import cn.bromine0x23.sgip.pdu.SgipBindResp;
import cn.bromine0x23.sgip.util.HexUtil;
import lombok.Getter;

/**
 * Thrown for exceptions occurring with the underlying Channel or TCP/IP
 * connection such as read/write errors, closed connections, etc.
 *
 * Its best to restart a session if this occurs.
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipBindException extends Exception {

	private static final long serialVersionUID = -5425808730170099729L;

	@Getter
	private final SgipBindResp bindResponse;

	public SgipBindException(SgipBindResp bindResponse) {
		super(buildErrorMessage(bindResponse));
		this.bindResponse = bindResponse;
	}

	private static String buildErrorMessage(SgipBindResp bindResponse) {
		if (bindResponse == null) {
			return "Bind request failed (response was null)";
		} else {
			return "Unable to bind [error: 0x" + HexUtil.toHexString(bindResponse.getResult()) + "]";
		}
	}
}
