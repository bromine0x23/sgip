/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.pdu;


import cn.bromine0x23.sgip.SgipConstants;

/**
 * 不完整请求PDU
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipPartialPdu extends SgipEmptyBody<SgipPartialPduResp> {

	public SgipPartialPdu(int commandId) {
		super(commandId, "Partial");
	}

	@Override
	public SgipPartialPduResp createResponse() {
		return new SgipPartialPduResp(getCommandId() | SgipConstants.PDU_CMD_ID_RESP_MASK);
	}

	@Override
	public Class<SgipPartialPduResp> getResponseClass() {
		return SgipPartialPduResp.class;
	}
}
