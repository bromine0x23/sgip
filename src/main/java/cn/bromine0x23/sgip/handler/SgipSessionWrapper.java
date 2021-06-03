/*
 * Copyright © 2017-2021 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.handler;

import cn.bromine0x23.sgip.SgipSessionChannelListener;
import cn.bromine0x23.sgip.pdu.SgipPdu;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 会话包装
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipSessionWrapper extends ChannelDuplexHandler {

	private final SgipSessionChannelListener listener;

	public SgipSessionWrapper(SgipSessionChannelListener listener) {
		this.listener = listener;
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
		if (object instanceof SgipPdu) {
			SgipPdu pdu = (SgipPdu)object;
			listener.pduReceived(pdu);
		}
		super.channelRead(context, object);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		listener.exceptionCaught(cause);
		super.exceptionCaught(context, cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext context) throws Exception {
		listener.channelInactive();
		super.channelInactive(context);
	}
}
