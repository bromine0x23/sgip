/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

/**
 * ChannelBuffer 字节编码工具
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@UtilityClass
public class ByteBufUtil {

	public static byte[] readBytes(ByteBuf buffer, int length) {
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		return bytes;
	}

	public static String readFixedString(ByteBuf buffer, int length) {
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		return new String(bytes).trim();
	}

	public static void writeFixedString(ByteBuf buffer, String s, int length) {
		if (s == null) {
			buffer.writeBytes(new byte[length]);
		} else {
			byte[] originalBytes = s.getBytes();
			byte[] bytes = new byte[length];
			System.arraycopy(originalBytes, 0, bytes, 0, Math.min(originalBytes.length, length));
			buffer.writeBytes(bytes);
		}
	}
}
