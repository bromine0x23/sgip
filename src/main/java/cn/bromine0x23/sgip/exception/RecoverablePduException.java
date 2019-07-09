/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
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
