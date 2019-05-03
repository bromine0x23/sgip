package cn.bromine0x23.sgip.pdu;

/**
 * 通用请求PDU
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("WeakerAccess")
public abstract class SgipPduRequest<TResponse extends SgipPduResponse> extends SgipPdu {

	public SgipPduRequest(int commandId, String name) {
		super(commandId, name, true);
	}

	/**
	 * @return 请求对应的响应 PDU
	 */
	public abstract TResponse createResponse();

	/**
	 * @return 响应 PDU 类
	 */
	public abstract Class<TResponse> getResponseClass();
}
