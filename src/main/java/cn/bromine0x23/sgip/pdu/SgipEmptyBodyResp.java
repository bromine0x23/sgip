/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.pdu;

import io.netty.buffer.ByteBuf;

/**
 * 空消息体响应
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
public abstract class SgipEmptyBodyResp extends SgipPduResponse {

	protected SgipEmptyBodyResp(int commandId, String name) {
		super(commandId, name);
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 0;
	}

	@Override
	public void readBody(ByteBuf buffer) {
	}

	@Override
	public void writeBody(ByteBuf buffer) {
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
	}
}
