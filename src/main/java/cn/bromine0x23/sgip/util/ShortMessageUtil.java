/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短消息工具
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
public class ShortMessageUtil {

	public static final int MAX_SEGMENT_LENGTH = 140;

	// private static final int MAX_SEGMENT_LENGTH = 134;

	private static final int MAX_SEGMENTS_COUNT = 255;

	private static final int HEADER_LENGTH = 6;

	private static final byte UDH_HEADER_LENGTH = 0x05;

	private static final byte UDH_IEI_CONCATENATED = 0x00; // UDH Information-Element-Identifier: Concatenated short messages, 8-bit reference number

	private static final byte UDH_IEI_CONCATENATED_LENGTH = 0x03;

	private static final int SEGMENT_HEADER_LENGTH = 1 + 2 + UDH_IEI_CONCATENATED_LENGTH;

	private static final AtomicInteger referenceCounter = new AtomicInteger();

	private ShortMessageUtil() {
	}

	public static byte[][] encode(String content, Charset charset) {
		return encode(content.getBytes(charset));
	}

	/**
	 * 按 3GPP TS 23.040／GSM 03.40 标准编码消息
	 *
	 * @param bytes 消息内容
	 * @return 编码后的数据段
	 */
	public static byte[][] encode(byte[] bytes) {
		if (bytes.length <= MAX_SEGMENT_LENGTH) {
			byte[][] segments = new byte[1][];
			segments[0] = bytes;
			return segments;
		}
		return encodeConcatenated(bytes);
	}

	/**
	 * 按 3GPP TS 23.040／GSM 03.40 标准编码消息，使用级联短消息（Concatenated Short Messages）
	 * <p>
	 * 参见 3GPP TS 23.040／GSM 03.40 9.2.3.24.1
	 *
	 * @param bytes 短消息内容
	 * @return 编码后的数据段
	 */
	private static byte[][] encodeConcatenated(byte[] bytes) {
		final byte     reference     = (byte)(referenceCounter.getAndIncrement() & 0xFF);
		final int      segmentsCount = getSegmentsCount(bytes);
		final byte[][] segments      = new byte[segmentsCount][];
		final int      length        = Math.min(bytes.length, segmentsCount * MAX_SEGMENTS_COUNT);
		for (int i = 0; i < segmentsCount; ++i) {
			int    segmentLength = Math.min(MAX_SEGMENT_LENGTH - SEGMENT_HEADER_LENGTH, length - i * (MAX_SEGMENT_LENGTH - SEGMENT_HEADER_LENGTH));
			byte[] segment       = new byte[HEADER_LENGTH + segmentLength];
			segment[0] = UDH_HEADER_LENGTH;
			segment[1] = UDH_IEI_CONCATENATED;
			segment[2] = UDH_IEI_CONCATENATED_LENGTH;
			segment[3] = reference; // Concatenated short message reference number.
			segment[4] = (byte)segmentsCount; // Maximum number of short messages in the concatenated short message.
			segment[5] = (byte)(i + 1); // Sequence number of the current short message.
			System.arraycopy(bytes, i * (MAX_SEGMENT_LENGTH - SEGMENT_HEADER_LENGTH), segment, HEADER_LENGTH, segmentLength);
			segments[i] = segment;
		}
		return segments;
	}

	private static int getSegmentsCount(byte[] bytes) {
		return Math.min((bytes.length + (MAX_SEGMENT_LENGTH - SEGMENT_HEADER_LENGTH) - 1) / (MAX_SEGMENT_LENGTH - SEGMENT_HEADER_LENGTH), MAX_SEGMENTS_COUNT);
	}
}
