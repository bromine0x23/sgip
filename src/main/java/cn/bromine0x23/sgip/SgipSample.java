/*
 * Copyright © 2017-2020 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.impl.DefaultSgipClient;
import cn.bromine0x23.sgip.impl.DefaultSgipSessionHandler;
import cn.bromine0x23.sgip.pdu.SgipSubmit;
import cn.bromine0x23.sgip.pdu.SgipSubmitResp;
import cn.bromine0x23.sgip.util.NamingThreadFactory;
import cn.bromine0x23.sgip.util.ShortMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试样例
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public abstract class SgipSample {

	private static final Logger logger = LoggerFactory.getLogger(SgipSample.class);

	public static void main(String[] arguments) throws Exception {
		if (arguments.length < 4) {
			throw new IllegalArgumentException("缺少参数，需要参数：<host> <port> <name> <password> <source> <target>");
		}

		String host     = arguments[0]; // 服务器地址
		int    port     = Integer.parseInt(arguments[1]); // 服务器端口
		String name     = arguments[2]; // 登录名
		String password = arguments[3]; // 密码
		String source   = arguments[4]; // 接收号码
		String target   = arguments[5]; // 接收号码

		// 创建客户端
		ExecutorService executors = Executors.newCachedThreadPool(new NamingThreadFactory("SgipSample"));
		SgipClient      client    = new DefaultSgipClient(executors);

		// 设置参数
		SgipSessionConfiguration configuration = new SgipSessionConfiguration();
		configuration.setHost(host);
		configuration.setPort(port);
		configuration.setSourceNodeId(0XFFFFFFFF);
		configuration.setLoginName(name);
		configuration.setLoginPassword(password);
		configuration.setLoginType(SgipLoginType.SP_TO_SMG);
		logger.info("configuration = {}", configuration);


		// 执行Bind操作，创建会话
		logger.info("=> session.bind");
		SgipSession session = client.bind(configuration, new DefaultSgipSessionHandler());
		logger.info("<= session.bind");

		SgipSubmitResp submitResp;

		// 测试短信发送
		String[] messages = {
			"Sgip协议测试",
			"我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。我能够吞下玻璃而不伤身体。"
		};
		for (String message : messages) {
			byte[][] contents = ShortMessageUtil.encode(message, SgipConstants.CHARSET_UCS2);
			byte tpUdhi = contents.length > 1 ? SgipConstants.TpUdhi.CONTAINS_HEADER : SgipConstants.TpUdhi.NORMAL;
			for (byte[] content : contents) {
				SgipSubmit submit = new SgipSubmit();
				submit.setSourceNodeId(0XFFFFFFFF);
				submit.setSpNumber(source);
				submit.setChargeNumber("000000000000000000000");
				submit.setUserNumber(target);
				submit.setCorporationId("00000");
				submit.setServiceType("00");
				submit.setFeeType(1);
				submit.setFeeValue(1);
				submit.setGivenValue(1);
				submit.setBillFlag(SgipConstants.BillFlag.RECEIVABLE);
				submit.setMoToMtFlag(SgipConstants.MoToMtFlag.NOT_UNICAST);
				submit.setPriority(0);
				submit.setExpireTime(null);
				submit.setScheduleTime(null);
				submit.setReportFlag(SgipConstants.ReportFlag.ERROR_ONLY);
				submit.setTpPid(SgipConstants.TpPid.NORMAL);
				submit.setMessageCoding(SgipConstants.DataCoding.UCS2); // USC2(UTF-16) 编码
				submit.setMessageType(SgipConstants.MessageType.SMS);
				submit.setTpUdhi(tpUdhi);
				submit.setMessageContent(content);
				logger.info("=> session.submit {}", submit);
				submitResp = session.submit(submit, 5000);
				logger.info("<= session.submit {}", submitResp);
			}
		}

		// 注销会话
		logger.info("=> session.unbind");
		session.unbind(5000);
		logger.info("<= session.unbind");


		// 关闭连接
		logger.info("=> session.destroy");
		session.destroy();
		logger.info("<= session.destroy");


		// 关闭客户端
		logger.info("=> client.destroy");
		client.destroy();
		logger.info("<= client.destroy");
	}
}
