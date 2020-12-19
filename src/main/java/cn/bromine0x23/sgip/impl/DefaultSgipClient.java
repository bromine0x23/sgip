/*
 * Copyright © 2017-2020 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.impl;

import cn.bromine0x23.sgip.handler.SgipSessionPduDecoder;
import cn.bromine0x23.sgip.handler.SgipSessionPduEncoder;
import cn.bromine0x23.sgip.handler.SgipSessionThreadRenamer;
import cn.bromine0x23.sgip.handler.SgipSessionWrapper;
import cn.bromine0x23.sgip.SgipClient;
import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.SgipSession;
import cn.bromine0x23.sgip.SgipSessionConfiguration;
import cn.bromine0x23.sgip.SgipSessionHandler;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.SgipBindException;
import cn.bromine0x23.sgip.exception.SgipChannelConnectException;
import cn.bromine0x23.sgip.exception.SgipChannelConnectTimeoutException;
import cn.bromine0x23.sgip.exception.SgipChannelException;
import cn.bromine0x23.sgip.exception.SgipTimeoutException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.pdu.SgipBind;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ConnectTimeoutException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SGIP 客户端实现
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@Slf4j
public class DefaultSgipClient implements SgipClient {

	private final EventLoopGroup workerGroup;

	private final Bootstrap bootstrap;

	private ScheduledExecutorService monitorExecutor;

	public DefaultSgipClient() {
		this(Executors.newCachedThreadPool());
	}

	public DefaultSgipClient(ExecutorService executors) {
		this(executors, Runtime.getRuntime().availableProcessors());
	}

	public DefaultSgipClient(ExecutorService executors, int expectedSessions) {
		this(executors, expectedSessions, null);
	}

	public DefaultSgipClient(ExecutorService executors, int expectedSessions, ScheduledExecutorService monitorExecutor) {
		this.workerGroup = new NioEventLoopGroup(expectedSessions, executors);
		this.bootstrap = new Bootstrap();
		this.monitorExecutor = monitorExecutor;
		bootstrap
			.group(workerGroup)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel channel) {
					channel.pipeline()
						.addLast(SgipConstants.PIPELINE_LOGGER_NAME, new LoggingHandler(LogLevel.TRACE))
					;
				}
			})
			.option(ChannelOption.SO_KEEPALIVE, true)
		;
	}

	@Override
	public void destroy() {
		workerGroup.shutdownGracefully();
	}

	@Override
	public SgipSession bind(
		SgipSessionConfiguration configuration,
		SgipSessionHandler sessionHandler
	) throws SgipTimeoutException, SgipChannelException, SgipBindException, UnrecoverablePduException, InterruptedException {
		DefaultSgipSession session = null;
		try {
			session = doOpen(configuration, sessionHandler);
			doBind(session, configuration);
		} finally {
			if (session != null && !session.isBound()) {
				try {
					session.close();
				} catch (Exception ignored) {
					// ignored
				}
			}
		}
		return session;
	}

	protected DefaultSgipSession doOpen(
		SgipSessionConfiguration configuration,
		SgipSessionHandler sessionHandler
	) throws SgipChannelException, InterruptedException {
		Channel channel = createConnectedChannel(configuration.getHost(), configuration.getPort(), configuration.getConnectTimeout());
		return createSession(channel, configuration, sessionHandler);
	}

	protected void doBind(
		DefaultSgipSession session,
		SgipSessionConfiguration configuration
	) throws SgipTimeoutException, SgipChannelException, SgipBindException, UnrecoverablePduException, InterruptedException {
		SgipBind bindRequest = createBindRequest(configuration);
		try {
			session.bind(bindRequest, configuration.getBindTimeout());
		} catch (RecoverablePduException exception) {
			throw new UnrecoverablePduException(exception.getMessage(), exception);
		}
	}

	protected Channel createConnectedChannel(String host, int port, int connectTimeoutMillis) throws InterruptedException, SgipChannelException {
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);
		ChannelFuture future = bootstrap.connect(host, port);
		future.awaitUninterruptibly();
		if (future.isCancelled()) {
			throw new InterruptedException("future cancelled by user");
		} else if (!future.isSuccess()) {
			if (future.cause() instanceof ConnectTimeoutException) {
				throw new SgipChannelConnectTimeoutException(
					"Unable to connect to host [" + host + "] and port [" + port + "] within " + connectTimeoutMillis + " ms", future.cause()
				);
			} else {
				throw new SgipChannelConnectException(
					"Unable to connect to host [" + host + "] and port [" + port + "]: " + future.cause().getMessage(), future.cause()
				);
			}
		}
		return future.channel();
	}

	protected DefaultSgipSession createSession(
		Channel channel,
		SgipSessionConfiguration configuration,
		SgipSessionHandler sessionHandler
	) {
		DefaultSgipSession session = new DefaultSgipSession(configuration, channel, sessionHandler, monitorExecutor);
		if (configuration.getName() != null) {
			channel.pipeline().addLast("sgipSessionThreadRenamer", new SgipSessionThreadRenamer(configuration.getName()));
		} else {
			log.warn("Session configuration did not have a name set - skipping threadRenamer in pipeline");
		}
		if (configuration.getWriteTimeout() > 0) {
			WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(configuration.getWriteTimeout(), TimeUnit.MILLISECONDS);
			channel.pipeline().addLast(SgipConstants.PIPELINE_SESSION_WRITE_TIMEOUT_NAME, writeTimeoutHandler);
		}
		channel.pipeline()
			.addLast(SgipConstants.PIPELINE_SESSION_PDU_DECODER_NAME, new SgipSessionPduDecoder())
			.addLast(SgipConstants.PIPELINE_SESSION_PDU_ENCODER_NAME, new SgipSessionPduEncoder())
			.addLast(SgipConstants.PIPELINE_SESSION_WRAPPER_NAME, new SgipSessionWrapper(session));
		return session;
	}

	private SgipBind createBindRequest(SgipSessionConfiguration configuration) {
		SgipBind bind = new SgipBind();
		bind.setSourceNodeId(configuration.getSourceNodeId());
		bind.setLoginName(configuration.getLoginName());
		bind.setLoginPassword(configuration.getLoginPassword());
		bind.setLoginType(configuration.getLoginType().getCode());
		return bind;
	}
}
