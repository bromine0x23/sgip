package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.pdu.SgipPdu;

/**
 * SGIP会话监听器接口
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipSessionListener extends SgipSessionHandler {

	boolean pduReceived(SgipPdu pdu);

	boolean pduDispatch(SgipPdu pdu);
}
