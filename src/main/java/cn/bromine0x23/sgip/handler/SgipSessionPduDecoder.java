package cn.bromine0x23.sgip.handler;

import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.pdu.SgipPdu;
import cn.bromine0x23.sgip.util.SgipPduCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SGIP PDU解码器
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipSessionPduDecoder extends ChannelInboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(SgipSessionPduDecoder.class);

	private final ByteBuf buffer = Unpooled.buffer();

	@Override
	public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
		logger.debug("read {}", object);
		try {
			if (object instanceof ByteBuf) {
				final ByteBuf buf = (ByteBuf)object;
				buffer.writeBytes(buf);
				while (buffer.readableBytes() >= SgipConstants.PDU_INT_LENGTH) {
					final int commandLength = buffer.getInt(buffer.readerIndex());
					if (buffer.readableBytes() < commandLength) {
						break;
					}
					final SgipPdu pdu = SgipPduCodec.decode(commandLength, buffer);
					context.fireChannelRead(pdu);
				}
				buffer.discardSomeReadBytes();
			}
		} finally {
			ReferenceCountUtil.release(object);
		}
	}
}
