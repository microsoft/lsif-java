/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

	public static enum IdType {
		UUID, COUNTER
	}

	private AtomicInteger counter = new AtomicInteger(0);

	public IdGenerator(IdType type) {
		this.idtype = type;
	}

	private IdType idtype;

	public String next() {
		switch (idtype) {
			case COUNTER:
				counter.incrementAndGet();
				return String.valueOf(counter);
			case UUID:
				return UUID.randomUUID().toString();
			default:
				break;
		}
		return null;
	}
}
