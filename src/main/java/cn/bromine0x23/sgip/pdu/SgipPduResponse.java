package cn.bromine0x23.sgip.pdu;

/**
 * 通用响应PDU
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public abstract class SgipPduResponse extends SgipPdu {

	public SgipPduResponse(int commandId, String name) {
		super(commandId, name, false);
	}
}
