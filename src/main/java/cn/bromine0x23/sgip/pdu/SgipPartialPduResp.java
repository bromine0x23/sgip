package cn.bromine0x23.sgip.pdu;

/**
 * 不完整响应PDU
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipPartialPduResp extends SgipEmptyBodyResp {

	public SgipPartialPduResp(int commandId) {
		super(commandId, "Partial_Resp");
	}
}
