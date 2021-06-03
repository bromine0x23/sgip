/*
 * Copyright © 2017-2021 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;

/**
 * 线程重命名
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipThreadRenamer extends ChannelInboundHandlerAdapter {

	@Getter
	@Setter
	private String threadName;

	public SgipThreadRenamer(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(threadName);
		super.channelRead(context, msg);
		Thread.currentThread().setName(currentThreadName);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext context) throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(threadName);
		super.channelReadComplete(context);
		Thread.currentThread().setName(currentThreadName);
	}
}
