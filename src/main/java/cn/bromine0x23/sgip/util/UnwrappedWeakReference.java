package cn.bromine0x23.sgip.util;

/*
 * #%L
 * ch-commons-util
 * %%
 * Copyright (C) 2012 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
