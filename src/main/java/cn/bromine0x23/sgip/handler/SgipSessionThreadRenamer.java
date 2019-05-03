package cn.bromine0x23.sgip.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;

/**
 * 线程重命名
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
public class SgipSessionThreadRenamer extends ChannelInboundHandlerAdapter {

	@Getter
	@Setter
	private String threadName;

	public SgipSessionThreadRenamer(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(threadName);
		super.channelRead(context, msg);
		Thread.currentThread().setName(currentThreadName);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext context) throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(threadName);
		super.channelReadComplete(context);
		Thread.currentThread().setName(currentThreadName);
	}
}
