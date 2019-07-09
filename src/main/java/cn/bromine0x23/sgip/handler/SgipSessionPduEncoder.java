/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.handler;

import cn.bromine0x23.sgip.pdu.SgipPdu;
import cn.bromine0x23.sgip.util.SgipPduCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * SGIP PDU编码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipSessionPduEncoder extends MessageToByteEncoder<SgipPdu> {

	@Override
	protected void encode(ChannelHandlerContext context, SgipPdu message, ByteBuf out) throws Exception {
		SgipPduCodec.encode(message, out);
	}
}
