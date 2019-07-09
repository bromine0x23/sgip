/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.util.ByteBufUtil;
import cn.bromine0x23.sgip.util.HexUtil;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用 Resp 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public abstract class SgipBasePduResp extends SgipPduResponse {

	/**
	 * 错误码（1 Byte）
	 */
	@Getter
	@Setter
	private byte result;

	/**
	 * 保留，扩展用（8 Byte）
	 */
	@Getter
	@Setter
	private String reserve = "";

	SgipBasePduResp(int commandId, String name) {
		super(commandId, name);
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 9;
	}

	@Override
	public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		this.result = buffer.readByte();
		this.reserve = ByteBufUtil.readFixedString(buffer, 8);
	}

	@Override
	public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		buffer.writeByte(result);
		ByteBufUtil.writeFixedString(buffer, reserve, 8);
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
		builder.append("(Result=");
		HexUtil.appendHexString(builder, getResult());
		builder.append(", Reserve=");
		builder.append(getReserve());
		builder.append(")");
	}
}
