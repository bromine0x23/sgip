/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip.util;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * Smarter WeakReference that "unwraps" the referenced object in a few methods
 * such as "equals()" which let a WeakReference be used in many other types of
 * collections and lists and have them still work correctly. For exmaple,
 * CopyOnWriteArrayList can be directly used with a UnwrappedWeakReference and
 * have methods like "addIfAbsent" actually work correctly.
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class UnwrappedWeakReference<T> extends WeakReference<T> {

	public UnwrappedWeakReference(T ref) {
		super(ref);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WeakReference) {
			object = ((WeakReference) object).get();
		}
		return Objects.equals(get(), object);

	}

	@Override
	public int hashCode() {
		T referent = get();
		return referent == null ? 0 : referent.hashCode();
	}
}
