/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.impl;

import cn.bromine0x23.sgip.SgipAsyncPduResponse;
import cn.bromine0x23.sgip.util.HexUtil;
import cn.bromine0x23.sgip.windowing.WindowFuture;
import cn.bromine0x23.sgip.pdu.SgipPduRequest;
import cn.bromine0x23.sgip.pdu.SgipPduResponse;

/**
 * 异步响应实现
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class DefaultSgipAsyncPduResponse implements SgipAsyncPduResponse {

	private final WindowFuture<Integer, SgipPduRequest, SgipPduResponse> future;

	public DefaultSgipAsyncPduResponse(WindowFuture<Integer, SgipPduRequest, SgipPduResponse> future) {
		this.future = future;
	}

	@Override
	public SgipPduRequest getRequest() {
		return future.getRequest();
	}

	@Override
	public SgipPduResponse getResponse() {
		return future.getResponse();
	}

	@Override
	public int getWindowSize() {
		return future.getWindowSize();
	}

	@Override
	public long getWindowWaitTime() {
		return future.getOfferToAcceptTime();
	}

	@Override
	public long getResponseTime() {
		return future.getAcceptToDoneTime();
	}

	@Override
	public long getEstimatedProcessingTime() {
		long responseTime = getResponseTime();
		if (responseTime == 0 || future.getWindowSize() == 0) {
			return 0;
		}
		return (responseTime / future.getWindowSize());
	}

	@Override
	public String toString() {
		//noinspection StringBufferReplaceableByString
		StringBuilder builder = new StringBuilder();
		builder.append("sgip_async_resp: sequenceNumber [0x");
		builder.append(HexUtil.toHexString(future.getKey()));
		builder.append("] windowSize [");
		builder.append(getWindowSize());
		builder.append("] windowWaitTime [");
		builder.append(getWindowWaitTime());
		builder.append(" ms] responseTime [");
		builder.append(getResponseTime());
		builder.append(" ms] estProcessingTime [");
		builder.append(getEstimatedProcessingTime());
		builder.append(" ms] reqType [");
		builder.append(getRequest().getName());
		builder.append("] respType [");
		builder.append(getResponse().getName());
		builder.append("]");
		return builder.toString();
	}
}
