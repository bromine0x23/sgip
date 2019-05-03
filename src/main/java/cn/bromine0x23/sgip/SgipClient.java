package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.exception.SgipBindException;
import cn.bromine0x23.sgip.exception.SgipChannelException;
import cn.bromine0x23.sgip.exception.SgipTimeoutException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;

/**
 * SGIP客户端接口
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipClient {

	SgipSession bind(
		SgipSessionConfiguration configuration,
		SgipSessionHandler sessionHandler
	) throws SgipTimeoutException, SgipChannelException, SgipBindException, UnrecoverablePduException, InterruptedException;

	void destroy();
}
