/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import java.util.concurrent.ThreadFactory;

/**
 * A ThreadFactory that standardizes the two most common customizations: thread
 * name and whether a thread will be a daemon.  Threads created by this factory
 * will always have the pattern "name-sequence".
 *
 * @author joelauer
 */
public class NamingThreadFactory implements ThreadFactory {

	private String    name;
	private boolean   daemon;
	private Sequencer sequencer;

	/**
	 * Creates a new NamingThreadFactory.  All threads will be non-daemon threads.
	 *
	 * @param name The name (prefix) of any threads created by this factory.
	 */
	public NamingThreadFactory(String name) {
		this(name, false);
	}

	/**
	 * Creates a new NamingThreadFactory.
	 *
	 * @param name   The name (prefix) of any threads created by this factory.
	 * @param daemon Whether to create a daemon thread.
	 */
	public NamingThreadFactory(String name, boolean daemon) {
		this.name = name;
		this.daemon = daemon;
		this.sequencer = new Sequencer();
	}

	@Override
	public Thread newThread(Runnable r) {
		long   id         = sequencer.next();
		String threadName = name + "-" + id;
		Thread thread     = new Thread(r, threadName);
		thread.setDaemon(daemon);
		return thread;
	}

}
