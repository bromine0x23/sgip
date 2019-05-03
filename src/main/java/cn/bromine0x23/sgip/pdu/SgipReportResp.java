package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import lombok.EqualsAndHashCode;

/**
 * Report_Resp 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipReportResp extends SgipBasePduResp {

	public SgipReportResp() {
		super(SgipConstants.COMMAND_ID_REPORT_RESP, "Report_Resp");
	}
}
