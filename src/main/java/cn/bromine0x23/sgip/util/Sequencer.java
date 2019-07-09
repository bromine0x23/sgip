/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * This class implements an extremely efficient, thread-safe way to generate a
 * simple incrementing sequence of Longs. This class safely resets the sequence
 * back to zero to prevent overflow. Internally uses the AtomicLong class.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class Sequencer {

	private AtomicLong sequenceNumber;

	/**
	 * Constructs a new instance of <code>Sequencer</code> a default starting
	 * sequence number of 0.
	 */
	public Sequencer() {
		this(0);
	}

	/**
	 * Constructs a new instance of <code>Sequencer</code> with the specified
	 * starting value.
	 */
	public Sequencer(long startingValue) {
		sequenceNumber = new AtomicLong(startingValue);
	}

	public long next() {
		long seqNum = sequenceNumber.getAndIncrement();
		// check if this value is getting close to overflow?
		if (seqNum > Long.MAX_VALUE - 1000000000) {
			sequenceNumber.set(0);
		}
		return seqNum;
	}

}
