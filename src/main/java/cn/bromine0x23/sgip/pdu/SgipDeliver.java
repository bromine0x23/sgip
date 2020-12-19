/*
 * Copyright © 2017-2020 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.util.ByteBufUtil;
import cn.bromine0x23.sgip.util.HexUtil;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Deliver 命令
 * <p>
 * <blockquote>
 * 在SP和SMG的通信中，SMG用Deliver命令向SP发送一条MO短消息。
 * SP接收到Deliver命令，会返回Deliver_Resp应答。
 * SMG根据Deliver命令中目的特服号，判断出该服务属于和哪一个SMG相连接的SP，如果属于本地SP，则直接发送到SP，否则路由至相应的SMG。
 * 在SMG和SMG的通信中，Deliver命令用于SMG客户端向服务器端路由MO短消息。
 * 服务器端接收到Deliver命令后，再发送到与之相连的目的SP。
 * </blockquote>
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipDeliver extends SgipPduRequest<SgipDeliverResp> {

	/**
	 * 发送短消息的用户手机号，手机号码前加“86”国别标志（21 Byte）
	 */
	@Getter
	@Setter
	private String userNumber;

	/**
	 * SP的接入号码（21 Byte）
	 */
	@Getter
	@Setter
	private String spNumber;

	/**
	 * GSM协议类型（1 Byte）
	 * <p>
	 * <blockquote>
	 * 详细解释请参考GSM03.40中的9.2.3.9
	 * </blockquote>
	 *
	 * @see SgipConstants.TpPid
	 */
	@Getter
	@Setter
	private byte tpPid = SgipConstants.TpPid.NORMAL;

	/**
	 * TP User Data Header Indicator (TP UDHI)（1 Byte）
	 * <p>
	 * <blockquote>
	 * 详细解释请参考 3GPP 23.040（GSM 03.40）的 9.2.3.23 节，仅使用1位，右对齐
	 * </blockquote>
	 *
	 * @see SgipConstants.TpUdhi
	 */
	@Getter
	@Setter
	private byte tpUdhi;

	/**
	 * 短消息编码格式（1 Byte）
	 * <p>
	 * <blockquote>
	 * 0：纯ASCII字符串
	 * 3：写卡操作
	 * 4：二进制编码
	 * 8：UCS2编码
	 * 15: GBK编码
	 * 其它参见 GSM 03.38（3GPP 23.038）第 4 节：SMS Data Coding Scheme
	 * </blockquote>
	 *
	 * @see SgipConstants.DataCoding
	 */
	@Getter
	@Setter
	private byte messageCoding;

	/**
	 * 信息类型（1 Byte）
	 * <p>
	 * <blockquote>
	 * 0-短消息信息
	 * 其它：待定
	 * </blockquote>
	 */
	@Getter
	@Setter
	private byte messageType = SgipConstants.MessageType.SMS;

	/**
	 * 短消息长度（4 Byte）
	 */
	@Getter
	@Setter
	private int messageLength;

	/**
	 * 短消息内容（{@link #messageLength} Byte）
	 */
	@Getter
	@Setter
	private String messageContent;

	/**
	 * 保留，扩展用（8 Byte）
	 */
	@Getter
	@Setter
	private String reserve = "";

	public SgipDeliver() {
		super(SgipConstants.CommandId.DELIVER, "Deliver");
	}

	@Override
	public SgipDeliverResp createResponse() {
		return new SgipDeliverResp();
	}

	@Override
	public Class<SgipDeliverResp> getResponseClass() {
		return SgipDeliverResp.class;
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 21 + 21 + 1 + 1 + 1 + 1 + 4 + messageLength + 8;
	}

	@Override
	public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		this.userNumber     = ByteBufUtil.readFixedString(buffer, 21);
		this.spNumber       = ByteBufUtil.readFixedString(buffer, 21);
		this.tpPid          = buffer.readByte();
		this.tpUdhi         = buffer.readByte();
		this.messageCoding  = buffer.readByte();
		this.messageType    = buffer.readByte();
		this.messageLength  = buffer.readInt();
		this.messageContent = ByteBufUtil.readFixedString(buffer, messageLength);
		this.reserve        = ByteBufUtil.readFixedString(buffer, 8);
	}

	@Override
	public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		ByteBufUtil.writeFixedString(buffer, userNumber, 21);
		ByteBufUtil.writeFixedString(buffer, spNumber, 21);
		buffer.writeByte(tpPid);
		buffer.writeByte(tpUdhi);
		buffer.writeByte(messageCoding);
		buffer.writeByte(messageType);
		buffer.writeInt(messageLength);
		buffer.writeBytes(messageContent.getBytes());
		ByteBufUtil.writeFixedString(buffer, reserve, 8);
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
		builder.append("(UserNumber=");
		builder.append(getUserNumber());
		builder.append(", SpNumber=");
		builder.append(getSpNumber());
		builder.append(", TpPid=0x");
		HexUtil.appendHexString(builder, getTpPid());
		builder.append(", TpUdhi=0x");
		HexUtil.appendHexString(builder, getTpUdhi());
		builder.append(", MessageCoding=0x");
		HexUtil.appendHexString(builder, getMessageCoding());
		builder.append(", MessageType=0x");
		HexUtil.appendHexString(builder, getMessageType());
		builder.append(", MessageLength=0x");
		HexUtil.appendHexString(builder, getMessageLength());
		builder.append(", MessageContent=");
		builder.append(getMessageContent());
		builder.append(", Reserve=");
		builder.append(getReserve());
		builder.append(")");
	}
}
