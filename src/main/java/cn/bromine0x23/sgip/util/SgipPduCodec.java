/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnknownCommandIdException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.pdu.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * PDU编解码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipPduCodec {

	private SgipPduCodec() {
	}

	/**
	 * 编码 SGIP PDU
	 *
	 * @param pdu PDU
	 * @return 存放编码结果的字节缓冲
	 * @throws UnrecoverablePduException 不可恢复的PDU编码错误
	 * @throws RecoverablePduException   可恢复的PDU编码错误
	 */
	public static ByteBuf encode(SgipPdu pdu) throws UnrecoverablePduException, RecoverablePduException {
		ByteBuf buffer = Unpooled.buffer(pdu.getCommandLength());
		encode(pdu, buffer);
		return buffer;
	}

	/**
	 * 编码 SGIP PDU
	 *
	 * @param pdu    PDU
	 * @param buffer 用于存放编码结果的字节缓冲
	 * @throws UnrecoverablePduException 不可恢复的PDU编码错误
	 * @throws RecoverablePduException   可恢复的PDU编码错误
	 */
	public static void encode(SgipPdu pdu, ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		if (!pdu.hasCommandLengthCalculated()) {
			pdu.calculateAndSetCommandLength();
		}
		pdu.writeHeader(buffer);
		pdu.writeBody(buffer);
	}

	/**
	 * SGIP PDU 解码
	 *
	 * @param buffer 字节缓冲
	 * @return 解码出 SGIP PDU
	 * @throws UnrecoverablePduException 不可恢复的PDU解码错误
	 * @throws RecoverablePduException   可恢复的PDU解码错误
	 */
	public static SgipPdu decode(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		if (buffer.readableBytes() < SgipConstants.PDU_INT_LENGTH) {
			return null;
		}

		int commandLength = buffer.getInt(buffer.readerIndex());

		if (commandLength < SgipConstants.PDU_HEADER_LENGTH) {
			throw new UnrecoverablePduException("Invalid PDU length [" + commandLength + "] parsed");
		}

		if (buffer.readableBytes() < commandLength) {
			return null;
		}

		return decode(commandLength, buffer.readSlice(commandLength));
	}

	/**
	 * SGIP PDU 解码
	 *
	 * @param commandLength 指令ID
	 * @param buffer        字节缓冲
	 * @return 解码出 SGIP PDU
	 * @throws UnrecoverablePduException 不可恢复的PDU解码错误
	 * @throws RecoverablePduException   可恢复的PDU解码错误
	 */
	public static SgipPdu decode(int commandLength, ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		buffer.skipBytes(SgipConstants.PDU_INT_LENGTH);

		int commandId      = buffer.readInt();
		int sourceNodeId   = buffer.readInt();
		int timestamp      = buffer.readInt();
		int sequenceNumber = buffer.readInt();

		SgipPdu pdu = isRequestCommandId(commandId) ? createPduRequest(commandId) : createPduResponse(commandId);

		pdu.setCommandLength(commandLength);
		pdu.setSourceNodeId(sourceNodeId);
		pdu.setTimestamp(timestamp);
		pdu.setSequenceNumber(sequenceNumber);

		if (pdu instanceof SgipPartialPdu) {
			throw new UnknownCommandIdException(pdu, "Unsupported or unknown PDU request commandId [0x" + HexUtil.toHexString(commandId) + "]");
		} else if (pdu instanceof SgipPartialPduResp) {
			throw new UnknownCommandIdException(pdu, "Unsupported or unknown PDU response commandId [0x" + HexUtil.toHexString(commandId) + "]");
		}

		pdu.readBody(buffer);

		return pdu;
	}

	public static boolean isRequestCommandId(int commandId) {
		return ((commandId & SgipConstants.PDU_CMD_ID_RESP_MASK) == 0);
	}

	public static boolean isResponseCommandId(int commandId) {
		return ((commandId & SgipConstants.PDU_CMD_ID_RESP_MASK) == SgipConstants.PDU_CMD_ID_RESP_MASK);
	}

	private static SgipPdu createPduRequest(int commandId) {
		switch (commandId) {
			case SgipConstants.COMMAND_ID_BIND:
				return new SgipBind();
			case SgipConstants.COMMAND_ID_UNBIND:
				return new SgipUnbind();
			case SgipConstants.COMMAND_ID_SUBMIT:
				return new SgipSubmit();
			case SgipConstants.COMMAND_ID_DELIVER:
				return new SgipDeliver();
			case SgipConstants.COMMAND_ID_REPORT:
				return new SgipReport();
			default:
				return new SgipPartialPdu(commandId);
		}
	}

	private static SgipPdu createPduResponse(int commandId) {
		switch (commandId) {
			case SgipConstants.COMMAND_ID_BIND_RESP:
				return new SgipBindResp();
			case SgipConstants.COMMAND_ID_UNBIND_RESP:
				return new SgipUnbindResp();
			case SgipConstants.COMMAND_ID_SUBMIT_RESP:
				return new SgipSubmitResp();
			case SgipConstants.COMMAND_ID_DELIVER_RESP:
				return new SgipDeliverResp();
			case SgipConstants.COMMAND_ID_REPORT_RESP:
				return new SgipReportResp();
			default:
				return new SgipPartialPduResp(commandId);
		}
	}
}
