/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
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
