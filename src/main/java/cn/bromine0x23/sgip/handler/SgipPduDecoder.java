/*
 * Copyright © 2017-2021 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.handler;

import cn.bromine0x23.sgip.pdu.SgipPdu;
import cn.bromine0x23.sgip.util.SgipPduCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SGIP PDU解码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipPduDecoder extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(SgipPduDecoder.class);

	@Override
	public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
		logger.trace("read {}", object);
		if (object instanceof ByteBuf) {
			final SgipPdu pdu = SgipPduCodec.decode((ByteBuf) object);
			context.fireChannelRead(pdu);
		}
	}
}
