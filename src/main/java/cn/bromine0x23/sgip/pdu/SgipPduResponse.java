/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.pdu;

/**
 * 通用响应PDU
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public abstract class SgipPduResponse extends SgipPdu {

	public SgipPduResponse(int commandId, String name) {
		super(commandId, name, false);
	}
}
