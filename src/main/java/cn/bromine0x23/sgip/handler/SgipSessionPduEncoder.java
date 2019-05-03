package cn.bromine0x23.sgip.handler;

import cn.bromine0x23.sgip.pdu.SgipPdu;
import cn.bromine0x23.sgip.util.SgipPduCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * SGIP PDU编码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipSessionPduEncoder extends MessageToByteEncoder<SgipPdu> {

	@Override
	protected void encode(ChannelHandlerContext context, SgipPdu message, ByteBuf out) throws Exception {
		SgipPduCodec.encode(message, out);
	}
}
