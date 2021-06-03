/*
 * Copyright © 2017-2021 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * SGIP常量
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@UtilityClass
public class SgipConstants {

	public static final int PDU_INT_LENGTH    = 4;
	public static final int PDU_HEADER_LENGTH = 20;

	public static final int PDU_CMD_ID_RESP_MASK = 0x80000000;

	public static final String PIPELINE_LOGGER_NAME                = "nettyLogger";
	public static final String PIPELINE_FRAME_DECODER_NAME         = "sgipFrameDecoder";
	public static final String PIPELINE_PDU_DECODER_NAME           = "sgipPduDecoder";
	public static final String PIPELINE_PDU_ENCODER_NAME           = "sgipPduEncoder";
	public static final String PIPELINE_SESSION_WRAPPER_NAME       = "sgipSessionWrapper";
	public static final String PIPELINE_SESSION_WRITE_TIMEOUT_NAME = "sgipSessionWriteTimeout";
	public static final String PIPELINE_THREAD_RENAMER             = "sgipThreadRenamer";

	public static final int  DEFAULT_CONNECT_TIMEOUT         = 10000;
	public static final long DEFAULT_BIND_TIMEOUT            = 5000;
	public static final long DEFAULT_WRITE_TIMEOUT           = 0;
	public static final long DEFAULT_REQUEST_EXPIRY_TIMEOUT  = -1;
	public static final long DEFAULT_WINDOW_WAIT_TIMEOUT     = 30000;
	public static final int  DEFAULT_WINDOW_SIZE             = 1;
	public static final long DEFAULT_WINDOW_MONITOR_INTERVAL = -1;  // disabled

	/**
	 * 消息ID
	 */
	@UtilityClass
	public static class CommandId {
		public static final int BIND               = 0x00000001;
		public static final int BIND_RESP          = 0x80000001;
		public static final int UNBIND             = 0x00000002;
		public static final int UNBIND_RESP        = 0x80000002;
		public static final int SUBMIT             = 0x00000003;
		public static final int SUBMIT_RESP        = 0x80000003;
		public static final int DELIVER            = 0x00000004;
		public static final int DELIVER_RESP       = 0x80000004;
		public static final int REPORT             = 0x00000005;
		public static final int REPORT_RESP        = 0x80000005;
		public static final int ADDSP              = 0x00000006;
		public static final int ADDSP_RESP         = 0x80000006;
		public static final int MODIFYSP           = 0x00000007;
		public static final int MODIFYSP_RESP      = 0x80000007;
		public static final int DELETESP           = 0x00000008;
		public static final int DELETESP_RESP      = 0x80000008;
		public static final int QUERYROUTE         = 0x00000009;
		public static final int QUERYROUTE_RESP    = 0x80000009;
		public static final int ADDTELESEG         = 0x0000000A;
		public static final int ADDTELESEG_RESP    = 0x8000000A;
		public static final int MODIFYTELESEG      = 0x0000000B;
		public static final int MODIFYTELESEG_RESP = 0x8000000B;
		public static final int DELETETELESEG      = 0x0000000C;
		public static final int DELETETELESEG_RESP = 0x8000000C;
		public static final int ADDSMG             = 0x0000000D;
		public static final int ADDSMG_RESP        = 0x8000000D;
		public static final int MODIFYSMG          = 0x0000000E;
		public static final int MODIFYSMG_RESP     = 0x8000000E;
		public static final int DELETESMG          = 0x0000000F;
		public static final int DELETESMG_RESP     = 0x8000000F;
		public static final int CHECKUSER          = 0x00000010;
		public static final int CHECKUSER_RESP     = 0x80000010;
		public static final int USERRPT            = 0x00000011;
		public static final int USERRPT_RESP       = 0x80000011;
		public static final int TRACE              = 0x00001000;
		public static final int TRACE_RESP         = 0x80001000;
	}

	/**
	 * 错误码
	 */
	@UtilityClass
	public static class ErrorCode {
		public static final byte OK                      = 0x00;
		public static final byte INVALID_LOGIN           = 0x01; // 非法登陆：登录名、口令出错、登录名与口令不符等
		public static final byte DUPLICATE_LOGIN         = 0x02; // 重复登陆：如在同一TCP/IP连接中连续两次以上请求登录
		public static final byte TOO_MANY_CONNECTION     = 0x03; // 连接过多：指单个节点要求同时建立的连接数过多（大于60）
		public static final byte INVALID_LOGIN_TYPE      = 0x04; // 登陆类型错误：指 Bind 命令中的 Login Type 字段出错
		public static final byte INVALID_PARAMETER       = 0x05; // 参数格式错误：指命令中参数值与参数类型不符或与协议规定的范围不符
		public static final byte INVALID_NUMBER          = 0x06; // 非法手机号码：一般是指ChargeNumber和UserNumber的填写不规范（以前协议中所有手机号码字段出现非86130号码或手机号码前未加“86”时都应报错）
		public static final byte INVALID_COMMAND_ID      = 0x07; // 消息ID错
		public static final byte INVALID_MESSAGE_LENGTH  = 0x08; // 消息长度错误
		public static final byte INVALID_SEQUENCE_NUMBER = 0x09; // 非法序列号：包括序列号重复、序列号格式错误等
		public static final byte INVALID_GNS_OPERATOR    = 0x0A; // 非法操作GNS
		public static final byte NODE_BUSY               = 0x0B; // 节点忙：指本节点存储队列满或其他原因，暂时不能提供服务的情况
		public static final byte NO_BUSINESS_CODE        = 0x0D; // 业务代码未分配：根据MT话单里的接入号和业务代码找不到对应的申报项。
	}

	/**
	 * 登录类型
	 */
	@UtilityClass
	public static class LoginType {
		public static final byte SP_TO_SMG  = 0x01; // SP向SMG建立的连接，用于发送命令
		public static final byte SMG_TO_SP  = 0x02; // SMG向SP建立的连接，用于发送命令
		public static final byte SMG_TO_SMG = 0x03; // SMG之间建立的连接，用于转发命令
		public static final byte SMG_TO_GNS = 0x04; // SMG向GNS建立的连接，用于路由表的检索和维护
		public static final byte GNS_TO_SMG = 0x05; // GNS向SMG建立的连接，用于路由表的更新
		public static final byte GNS_TO_GNS = 0x06; // 主备GNS之间建立的连接，用于主备路由表的一致性
		public static final byte TEST       = 0x0B; // SP与SMG以及SMG之间建立的测试连接，用于跟踪测试
	}

	/**
	 * 代收费标志
	 */
	@UtilityClass
	public static class BillFlag {
		public static final byte RECEIVABLE = 0x00; // 应收
		public static final byte ACTUAL     = 0x01; // 实收
	}

	/**
	 * 引起MT消息的原因
	 */
	@UtilityClass
	public static class MoToMtFlag {
		public static final byte UNICAST_FIRST = 0x00; // MO点播引起的第一条MT消息
		public static final byte UNICAST_OTHER = 0x01; // MO点播引起的非第一条MT消息
		public static final byte NOT_UNICAST   = 0x02; // 非MO点播引起的MT消息
		public static final byte FEEDBACK      = 0x03; // 系统反馈引起的MT消息
	}

	/**
	 * 状态报告标记
	 */
	@UtilityClass
	public static class ReportFlag {
		public static final byte ERROR_ONLY = 0x00; // 该条消息只有最后出错时要返回状态报告
		public static final byte ALWAYS     = 0x01; // 该条消息无论最后是否成功都要返回状态报告
		public static final byte NONE       = 0x02; // 该条消息不需要返回状态报告
		public static final byte FEE_ONLY   = 0x03; // 该条消息仅携带包月计费信息，不下发给用户，要返回状态报告
	}

	/**
	 * 信息类型
	 */
	@UtilityClass
	public static class MessageType {
		public static final byte SMS = 0x00; // 短消息信息
	}

	/**
	 * Transfer Protocol Protocol Identifier (TP-PID)
	 *
	 * @see "3GPP TS 23.040（GSM 03.40）第 9.2.3.9 节"
	 * @see <a href="https://en.wikipedia.org/wiki/GSM_03.40#Protocol_Identifier">GSM 03.40 - Protocol Identifier (Wikipedia)</a>
	 */
	@UtilityClass
	public static class TpPid {
		public static final byte NORMAL = 0x00;
	}

	/**
	 * Transfer Protocol User Data Header Indicator (TP-UDHI)
	 *
	 * @see "3GPP TS 23.040（GSM 03.40）第 9.2.3.23 节"
	 */
	@UtilityClass
	public static class TpUdhi {
		public static final byte NORMAL          = 0b0; // 消息内容不包含额外头
		public static final byte CONTAINS_HEADER = 0b1; // 消息内容包含额外头
	}

	/**
	 * Data Coding - 消息编码格式
	 *
	 * @see "3GPP TS 23.038（GSM 03.38）第 4 节"
	 * @see <a href="https://en.wikipedia.org/wiki/Data_Coding_Scheme">Data Coding Scheme (Wikipedia)</a>
	 */
	@UtilityClass
	public static class DataCoding {
		public static final byte ASCII  = 0b00000000; // GSM 7 bit
		public static final byte BINARY = 0b00000100; // 8 bit data
		public static final byte UCS2   = 0b00001000; // UCS2
		public static final byte GBK    = 0b00001111; // GSM 03.38 保留
	}

	/*
	 * 常用字符编码
	 */
	public static final Charset CHARSET_ASCII   = StandardCharsets.US_ASCII;
	public static final Charset CHARSET_UCS2    = StandardCharsets.UTF_16BE;
	public static final Charset CHARSET_GBK     = Charset.forName("GBK");
	public static final Charset CHARSET_GB18030 = Charset.forName("GB18030");

}
