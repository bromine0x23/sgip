package cn.bromine0x23.sgip;

import cn.bromine0x23.sgip.pdu.SgipPdu;

/**
 * SGIP会话通道监听器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public interface SgipSessionChannelListener {

	void pduReceived(SgipPdu pdu);

	void exceptionCaught(Throwable throwable);

	void channelInactive();
}
