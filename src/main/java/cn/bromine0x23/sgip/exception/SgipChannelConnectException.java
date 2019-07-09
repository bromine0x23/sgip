/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.exception;

/**
 * Thrown for exceptions occuring while attempting to connect to a remote
 * system and cannot complete within a period of time.
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipChannelConnectException extends SgipChannelException {

	private static final long serialVersionUID = -7539271123711267704L;

	public SgipChannelConnectException(String message) {
		super(message);
	}

	public SgipChannelConnectException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
