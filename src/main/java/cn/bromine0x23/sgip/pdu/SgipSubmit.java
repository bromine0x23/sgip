/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
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

import java.util.Arrays;

/**
 * Submit 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipSubmit extends SgipPduRequest<SgipSubmitResp> {

	/**
	 * SP 接入号码（21 Byte）
	 */
	@Getter
	@Setter
	private String spNumber;

	/**
	 * 付费号码（21 Byte）
	 * <p>
	 * 手机号码前加“86”国别标志；
	 * 当且仅当群发且对用户收费时为空；
	 * 如果为空，则该条短消息产生的费用由UserNumber代表的用户支付；
	 * 如果为全零字符串“000000000000000000000”，表示该条短消息产生的费用由SP支付。
	 */
	@Getter
	@Setter
	private String chargeNumber = "000000000000000000000";

	/**
	 * 接收短消息的手机数量，取值范围1至100（1 Byte）
	 */
	@Getter
	private int userCount;

	/**
	 * 接收该短消息的手机号（21 Byte × {@code userCount}）
	 * <p>
	 * 该字段重复UserCount指定的次数，手机号码前加“86”国别标志
	 */
	@Getter
	private String[] userNumbers;

	/**
	 * 企业代码（5 Byte）
	 */
	@Getter
	@Setter
	private String corporationId;

	/**
	 * 业务代码（10 Byte）
	 */
	@Getter
	@Setter
	private String serviceType;

	/**
	 * 计费类型（1 Byte）
	 */
	@Getter
	private byte feeType;

	/**
	 * 该条短消息的收费值（6 Byte）
	 * <p>
	 * 取值范围0-99999，单位为分，由SP定义，对于包月制收费的用户，该值为月租费的值
	 */
	@Getter
	private int feeValue;

	/**
	 * 赠送用户的话费（6 Byte）
	 * <p>
	 * 取值范围0-99999，单位为分，由SP定义，特指由SP向用户发送广告时的赠送话费
	 */
	@Getter
	private int givenValue;

	/**
	 * 代收费标志（1 Byte）
	 * <p>
	 * 0：应收
	 * 1：实收
	 */
	@Getter
	@Setter
	private byte billFlag;

	/**
	 * 引起MT消息的原因（1 Byte）
	 * <p>
	 * 0: MO点播引起的第一条MT消息
	 * 1: MO点播引起的非第一条MT消息
	 * 2: 非MO点播引起的MT消息
	 * 3: 系统反馈引起的MT消息
	 */
	@Getter
	@Setter
	private byte moToMtFlag;

	/**
	 * 优先级（1 Byte）
	 * <p>
	 * 0-9从低到高，默认为0
	 */
	@Getter
	private byte priority = 0;

	/**
	 * 短消息寿命的终止时间（16 Byte）
	 * <p>
	 * 如果为空，表示使用短消息中心的缺省值。
	 * 时间内容为16个字符，格式为”yymmddhhmmsstnnp” ，其中“tnnp”取固定值“032+”，即默认系统为北京时间
	 */
	@Getter
	@Setter
	private String expireTime;

	/**
	 * 短消息定时发送的时间（16 Byte）
	 * <p>
	 * 如果为空，表示立刻发送该短消息。
	 * 时间内容为16个字符，格式为“yymmddhhmmsstnnp” ，其中“tnnp”取固定值“032+”，即默认系统为北京时间
	 */
	@Getter
	@Setter
	private String scheduleTime;

	/**
	 * 状态报告标记（1 Byte）
	 * <p>
	 * 0: 该条消息只有最后出错时要返回状态报告
	 * 1: 该条消息无论最后是否成功都要返回状态报告
	 * 2: 该条消息不需要返回状态报告
	 * 3: 该条消息仅携带包月计费信息，不下发给用户，要返回状态报告
	 * 其它: 保留
	 * 缺省设置为0
	 */
	@Getter
	@Setter
	private byte reportFlag = SgipConstants.REPORT_FLAG_ERROR_ONLY;

	/**
	 * GSM协议类型（1 Byte）
	 * <p>
	 * 详细解释请参考GSM03.40中的9.2.3.9
	 */
	@Getter
	@Setter
	private byte tpPid = SgipConstants.TP_PID_NORMAL;

	/**
	 * TP User Data Header Indicator (TP UDHI)（1 Byte）
	 * <p>
	 * 详细解释请参考 3GPP 23.040（GSM 03.40）的 9.2.3.23 节，仅使用1位，右对齐
	 */
	@Getter
	@Setter
	private byte tpUdhi;

	/**
	 * 短消息编码格式（1 Byte）
	 * <p>
	 * 0：纯ASCII字符串
	 * 3：写卡操作
	 * 4：二进制编码
	 * 8：UCS2编码
	 * 15: GBK编码
	 * 其它参见GSM3.38第4节：SMS Data Coding Scheme
	 */
	@Getter
	@Setter
	private byte messageCoding;

	/**
	 * 信息类型（1 Byte）
	 * <p>
	 * 0: 短消息信息
	 * 其它：待定
	 */
	@Getter
	@Setter
	private byte messageType = SgipConstants.MESSAGE_TYPE_SMS;

	/**
	 * 短消息长度（4 Byte）
	 */
	@Getter
	@Setter
	private int messageLength;

	/**
	 * 短消息内容（{@code messageLength} Byte）
	 */
	@Getter
	private byte[] messageContent;

	/**
	 * 保留，扩展用（8 Byte）
	 */
	@Getter
	@Setter
	private String reserve;

	public SgipSubmit() {
		super(SgipConstants.COMMAND_ID_SUBMIT, "Submit");
	}

	public void setUserNumber(String userNumber) {
		this.userCount = 1;
		this.userNumbers = new String[]{userNumber};
	}

	public void setUserNumbers(String[] userNumbers) {
		if (userNumbers.length > 100) {
			throw new IllegalArgumentException("Too many user numbers.");
		}
		this.userCount = userNumbers.length;
		this.userNumbers = userNumbers;
	}

	public void setFeeType(int feeType) {
		this.feeType = (byte)feeType;
	}

	public void setFeeValue(int feeValue) {
		if (feeValue < 0 || feeValue > 99999) {
			throw new IllegalArgumentException("Given `FeeValue` must between 0 and 99999.");
		}
		this.feeValue = feeValue;
	}

	public void setGivenValue(int givenValue) {
		if (givenValue < 0 || givenValue > 99999) {
			throw new IllegalArgumentException("Given `GivenValue` must between 0 and 99999.");
		}
		this.givenValue = givenValue;
	}

	public void setPriority(int priority) {
		if (priority < 0 || priority > 9) {
			throw new IllegalArgumentException("Priority for Submit must between 0 and 9.");
		}
		this.priority = (byte)priority;
	}

	public void setMessageContent(byte[] messageContent) {
		this.messageLength = messageContent.length;
		this.messageContent = messageContent;
	}

	@Override
	public SgipSubmitResp createResponse() {
		return new SgipSubmitResp();
	}

	@Override
	public Class<SgipSubmitResp> getResponseClass() {
		return SgipSubmitResp.class;
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 21 + 21 + 1 + 21 * userCount + 5 + 10 + 1 + 6 + 6 + 1 + 1 + 1 + 16 + 16 + 1 + 1 + 1 + 1 + 1 + 4 + messageLength + 8;
	}

	@Override
	public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		this.spNumber = ByteBufUtil.readFixedString(buffer, 21);
		this.chargeNumber = ByteBufUtil.readFixedString(buffer, 21);
		this.userCount = buffer.readByte();
		this.userNumbers = new String[userCount];
		for (int i = 0; i < userCount; ++i) {
			userNumbers[i] = ByteBufUtil.readFixedString(buffer, 21);
		}
		this.corporationId = ByteBufUtil.readFixedString(buffer, 5);
		this.serviceType = ByteBufUtil.readFixedString(buffer, 10);
		this.feeType = buffer.readByte();
		this.feeValue = Integer.parseInt(ByteBufUtil.readFixedString(buffer, 6));
		this.givenValue = Integer.parseInt(ByteBufUtil.readFixedString(buffer, 6));
		this.billFlag = buffer.readByte();
		this.moToMtFlag = buffer.readByte();
		this.priority = buffer.readByte();
		this.expireTime = ByteBufUtil.readFixedString(buffer, 16);
		this.scheduleTime = ByteBufUtil.readFixedString(buffer, 16);
		this.reportFlag = buffer.readByte();
		this.tpPid = buffer.readByte();
		this.tpUdhi = buffer.readByte();
		this.messageCoding = buffer.readByte();
		this.messageType = buffer.readByte();
		this.messageLength = buffer.readInt();
		this.messageContent = ByteBufUtil.readBytes(buffer, messageLength);
		this.reserve = ByteBufUtil.readFixedString(buffer, 8);
	}

	@Override
	public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		ByteBufUtil.writeFixedString(buffer, getSpNumber(), 21);
		ByteBufUtil.writeFixedString(buffer, getChargeNumber(), 21);
		buffer.writeByte(userCount);
		for (String userNumber : getUserNumbers()) {
			ByteBufUtil.writeFixedString(buffer, userNumber, 21);
		}
		ByteBufUtil.writeFixedString(buffer, getCorporationId(), 5);
		ByteBufUtil.writeFixedString(buffer, getServiceType(), 10);
		buffer.writeByte(getFeeType());
		ByteBufUtil.writeFixedString(buffer, Integer.toString(getFeeValue()), 6);
		ByteBufUtil.writeFixedString(buffer, Integer.toString(getGivenValue()), 6);
		buffer.writeByte(getBillFlag());
		buffer.writeByte(getMoToMtFlag());
		buffer.writeByte(getPriority());
		ByteBufUtil.writeFixedString(buffer, getExpireTime(), 16);
		ByteBufUtil.writeFixedString(buffer, getScheduleTime(), 16);
		buffer.writeByte(getReportFlag());
		buffer.writeByte(getTpPid());
		buffer.writeByte(getTpUdhi());
		buffer.writeByte(getMessageCoding());
		buffer.writeByte(getMessageType());
		buffer.writeInt(getMessageLength());
		buffer.writeBytes(getMessageContent());
		ByteBufUtil.writeFixedString(buffer, reserve, 8);
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
		builder.append("(SpNumber=");
		builder.append(getSpNumber());
		builder.append(", ChargeNumber=");
		builder.append(getChargeNumber());
		builder.append(", UserCount=");
		builder.append(getUserCount());
		builder.append(", UserNumbers=[");
		for (String userNumber : getUserNumbers()) {
			builder.append(userNumber);
			builder.append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		builder.append("], CorporationId=");
		builder.append(getCorporationId());
		builder.append(", ServiceType=");
		builder.append(getServiceType());
		builder.append(", FeeType=0x");
		HexUtil.appendHexString(builder, getFeeType());
		builder.append(", FeeValue=");
		builder.append(getFeeValue());
		builder.append(", GivenValue=");
		builder.append(getGivenValue());
		builder.append(", BillFlag=0x");
		HexUtil.appendHexString(builder, getBillFlag());
		builder.append(", MoToMtFlag=0x");
		HexUtil.appendHexString(builder, getMoToMtFlag());
		builder.append(", Priority=");
		builder.append(getPriority());
		builder.append(", ExpireTime=");
		builder.append(getExpireTime());
		builder.append(", ScheduleTime=");
		builder.append(getScheduleTime());
		builder.append(", ReportFlag=0x");
		HexUtil.appendHexString(builder, getReportFlag());
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
		builder.append(Arrays.toString(getMessageContent()));
		builder.append(", Reserve=");
		builder.append(getReserve());
		builder.append(")");

	}
}
