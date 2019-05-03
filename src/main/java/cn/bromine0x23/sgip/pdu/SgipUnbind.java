package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import lombok.EqualsAndHashCode;

/**
 * Unbind 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipUnbind extends SgipEmptyBody<SgipUnbindResp> {

	public SgipUnbind() {
		super(SgipConstants.COMMAND_ID_UNBIND, "Unbind");
	}

	@Override
	public SgipUnbindResp createResponse() {
		return new SgipUnbindResp();
	}

	@Override
	public Class<SgipUnbindResp> getResponseClass() {
		return SgipUnbindResp.class;
	}
}
