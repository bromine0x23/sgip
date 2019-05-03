package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.pdu.SgipPduRequest;
import cn.bromine0x23.sgip.pdu.SgipPduResponse;

/**
 * 异步响应接口
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipAsyncPduResponse {

	SgipPduRequest getRequest();

	SgipPduResponse getResponse();

	int getWindowSize();

	long getWindowWaitTime();

	long getResponseTime();

	long getEstimatedProcessingTime();
}
