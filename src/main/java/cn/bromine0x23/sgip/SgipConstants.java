/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;

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
	public static final String PIPELINE_SESSION_PDU_ENCODER_NAME   = "sgipSessionPduDecoder";
	public static final String PIPELINE_SESSION_PDU_DECODER_NAME   = "sgipSessionPduEncoder";
	public static final String PIPELINE_SESSION_WRAPPER_NAME       = "sgipSessionWrapper";
	public static final String PIPELINE_SESSION_WRITE_TIMEOUT_NAME = "sgipSessionWriteTimeout";

	public static final int  DEFAULT_CONNECT_TIMEOUT         = 10000;
	public static final long DEFAULT_BIND_TIMEOUT            = 5000;
	public static final long DEFAULT_WRITE_TIMEOUT           = 0;
	public static final long DEFAULT_REQUEST_EXPIRY_TIMEOUT  = -1;
	public static final long DEFAULT_WINDOW_WAIT_TIMEOUT     = 30000;
	public static final int  DEFAULT_WINDOW_SIZE             = 1;
	public static final long DEFAULT_WINDOW_MONITOR_INTERVAL = -1;  // disabled

	/*
	 * 消息ID
	 */
	public static final int COMMAND_ID_BIND               = 0x00000001;
	public static final int COMMAND_ID_BIND_RESP          = 0x80000001;
	public static final int COMMAND_ID_UNBIND             = 0x00000002;
	public static final int COMMAND_ID_UNBIND_RESP        = 0x80000002;
	public static final int COMMAND_ID_SUBMIT             = 0x00000003;
	public static final int COMMAND_ID_SUBMIT_RESP        = 0x80000003;
	public static final int COMMAND_ID_DELIVER            = 0x00000004;
	public static final int COMMAND_ID_DELIVER_RESP       = 0x80000004;
	public static final int COMMAND_ID_REPORT             = 0x00000005;
	public static final int COMMAND_ID_REPORT_RESP        = 0x80000005;
	public static final int COMMAND_ID_ADDSP              = 0x00000006;
	public static final int COMMAND_ID_ADDSP_RESP         = 0x80000006;
	public static final int COMMAND_ID_MODIFYSP           = 0x00000007;
	public static final int COMMAND_ID_MODIFYSP_RESP      = 0x80000007;
	public static final int COMMAND_ID_DELETESP           = 0x00000008;
	public static final int COMMAND_ID_DELETESP_RESP      = 0x80000008;
	public static final int COMMAND_ID_QUERYROUTE         = 0x00000009;
	public static final int COMMAND_ID_QUERYROUTE_RESP    = 0x80000009;
	public static final int COMMAND_ID_ADDTELESEG         = 0x0000000A;
	public static final int COMMAND_ID_ADDTELESEG_RESP    = 0x8000000A;
	public static final int COMMAND_ID_MODIFYTELESEG      = 0x0000000B;
	public static final int COMMAND_ID_MODIFYTELESEG_RESP = 0x8000000B;
	public static final int COMMAND_ID_DELETETELESEG      = 0x0000000C;
	public static final int COMMAND_ID_DELETETELESEG_RESP = 0x8000000C;
	public static final int COMMAND_ID_ADDSMG             = 0x0000000D;
	public static final int COMMAND_ID_ADDSMG_RESP        = 0x8000000D;
	public static final int COMMAND_ID_MODIFYSMG          = 0x0000000E;
	public static final int COMMAND_ID_MODIFYSMG_RESP     = 0x8000000E;
	public static final int COMMAND_ID_DELETESMG          = 0x0000000F;
	public static final int COMMAND_ID_DELETESMG_RESP     = 0x8000000F;
	public static final int COMMAND_ID_CHECKUSER          = 0x00000010;
	public static final int COMMAND_ID_CHECKUSER_RESP     = 0x80000010;
	public static final int COMMAND_ID_USERRPT            = 0x00000011;
	public static final int COMMAND_ID_USERRPT_RESP       = 0x80000011;
	public static final int COMMAND_ID_TRACE              = 0x00001000;
	public static final int COMMAND_ID_TRACE_RESP         = 0x80001000;

	/*
	 * 错误码
	 */
	public static final byte ERROR_CODE_OK                      = 0x00;
	public static final byte ERROR_CODE_INVALID_LOGIN           = 0x01; // 非法登陆：登录名、口令出错、登录名与口令不符等
	public static final byte ERROR_CODE_DUPLICATE_LOGIN         = 0x02; // 重复登陆：如在同一TCP/IP连接中连续两次以上请求登录
	public static final byte ERROR_CODE_TOO_MANY_CONNECTION     = 0x03; // 连接过多：指单个节点要求同时建立的连接数过多（大于60）
	public static final byte ERROR_CODE_INVALID_LOGIN_TYPE      = 0x04; // 登陆类型错误：指 Bind 命令中的 Login Type 字段出错
	public static final byte ERROR_CODE_INVALID_PARAMETER       = 0x05; // 参数格式错误：指命令中参数值与参数类型不符或与协议规定的范围不符
	public static final byte ERROR_CODE_INVALID_NUMBER          = 0x06; // 非法手机号码：一般是指ChargeNumber和UserNumber的填写不规范（以前协议中所有手机号码字段出现非86130号码或手机号码前未加“86”时都应报错）
	public static final byte ERROR_CODE_INVALID_COMMAND_ID      = 0x07; // 消息ID错
	public static final byte ERROR_CODE_INVALID_MESSAGE_LENGTH  = 0x08; // 消息长度错误
	public static final byte ERROR_CODE_INVALID_SEQUENCE_NUMBER = 0x09; // 非法序列号：包括序列号重复、序列号格式错误等
	public static final byte ERROR_CODE_INVALID_GNS_OPERATOR    = 0x0A; // 非法操作GNS
	public static final byte ERROR_CODE_NODE_BUSY               = 0x0B; // 节点忙：指本节点存储队列满或其他原因，暂时不能提供服务的情况
	public static final byte ERROR_CODE_NO_BUSINESS_CODE        = 0x0D; // 业务代码未分配：根据MT话单里的接入号和业务代码找不到对应的申报项。

	/*
	 * 登录类型
	 */
	public static final byte LOGIN_TYPE_SP_TO_SMG  = 0x01; // SP向SMG建立的连接，用于发送命令
	public static final byte LOGIN_TYPE_SMG_TO_SP  = 0x02; // SMG向SP建立的连接，用于发送命令
	public static final byte LOGIN_TYPE_SMG_TO_SMG = 0x03; // SMG之间建立的连接，用于转发命令
	public static final byte LOGIN_TYPE_SMG_TO_GNS = 0x04; // SMG向GNS建立的连接，用于路由表的检索和维护
	public static final byte LOGIN_TYPE_GNS_TO_SMG = 0x05; // GNS向SMG建立的连接，用于路由表的更新
	public static final byte LOGIN_TYPE_GNS_TO_GNS = 0x06; // 主备GNS之间建立的连接，用于主备路由表的一致性
	public static final byte LOGIN_TYPE_TEST       = 0x0B; // SP与SMG以及SMG之间建立的测试连接，用于跟踪测试

	/*
	 * 代收费标志
	 */
	public static final byte BILL_TYPE_RECEIVABLE = 0x00; // 应收
	public static final byte BILL_TYPE_ACTUAL     = 0x01; // 实收

	/*
	 * 引起MT消息的原因
	 */
	public static final byte MO_TO_MT_FLAG_UNICAST_FIRST = 0x00; // MO点播引起的第一条MT消息
	public static final byte MO_TO_MT_FLAG_UNICAST_OTHER = 0x01; // MO点播引起的非第一条MT消息
	public static final byte MO_TO_MT_FLAG_NOT_UNICAST   = 0x02; // 非MO点播引起的MT消息
	public static final byte MO_TO_MT_FLAG_FEEDBACK      = 0x03; // 系统反馈引起的MT消息

	/*
	 * 状态报告标记
	 */
	public static final byte REPORT_FLAG_ERROR_ONLY = 0x00; // 该条消息只有最后出错时要返回状态报告
	public static final byte REPORT_FLAG_ALWAYS     = 0x01; // 该条消息无论最后是否成功都要返回状态报告
	public static final byte REPORT_FLAG_NONE       = 0x02; // 该条消息不需要返回状态报告
	public static final byte REPORT_FLAG_FEE_ONLY   = 0x03; // 该条消息仅携带包月计费信息，不下发给用户，要返回状态报告

	/*
	 * TP Protocol Identifier (TP PID)
	 *
	 * 参见 3GPP TS 23.040（GSM 03.40）第 9.2.3.9 节
	 */
	public static final byte TP_PID_NORMAL = 0x00;

	/*
	 * TP User Data Header Indicator (TP UDHI)
	 *
	 * 参见 3GPP TS 23.040（GSM 03.40）第 9.2.3.23 节
	 */
	public static final byte TP_UDHI_NORMAL          = 0x00; // 消息内容不包含额外头
	public static final byte TP_UDHI_CONTAINS_HEADER = 0x01; // 消息内容包含额外头

	/*
	 * 消息编码格式
	 *
	 * 参见 3GPP TS 23.038（GSM 03.38）第 4 节
	 */
	public static final byte MESSAGE_CODING_ASCII  = 0x00; // 0000 0000
	public static final byte MESSAGE_CODING_BINARY = 0x04; // 0000 0100
	public static final byte MESSAGE_CODING_UCS2   = 0x08; // 0000 1000
	public static final byte MESSAGE_CODING_GBK    = 0x0F; // 0000 1111

	/*
	 * 常用字符集
	 */
	public static final Charset CHARSET_ASCII = Charset.forName("US-ASCII");
	public static final Charset CHARSET_UCS2  = Charset.forName("UTF-16BE");
	public static final Charset CHARSET_GBK   = Charset.forName("GBK");

	/*
	 * 信息类型
	 */
	public static final byte MESSAGE_TYPE_SMS = 0x00; // 短消息信息


}
