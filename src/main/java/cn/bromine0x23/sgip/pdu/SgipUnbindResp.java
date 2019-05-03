package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import lombok.EqualsAndHashCode;

/**
 * Unbind_Resp 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipUnbindResp extends SgipEmptyBodyResp {

	public SgipUnbindResp() {
		super(SgipConstants.COMMAND_ID_UNBIND_RESP, "Unbind_Resp");
	}
}
