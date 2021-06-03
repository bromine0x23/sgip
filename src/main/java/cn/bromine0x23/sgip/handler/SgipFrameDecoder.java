/*
 * Copyright © 2017-2021 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.handler;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * SGIP 帧解码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipFrameDecoder extends LengthFieldBasedFrameDecoder {

	public SgipFrameDecoder() {
		super(0x10_0000, 0, 4, 0, 0);
	}

}
