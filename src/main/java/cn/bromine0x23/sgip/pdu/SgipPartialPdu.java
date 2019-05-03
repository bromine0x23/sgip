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
