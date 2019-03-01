/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.UUID;

public class IdGenerator {

	public static enum IdType {
		UUID, COUNTER
	}

	private int counter = 0;

	public IdGenerator() {
		this.idtype = IdType.COUNTER;
	}

	private IdType idtype;

	public String next() {
		switch (idtype) {
		case COUNTER:
			counter++;
			return String.valueOf(counter);
		case UUID:
			return UUID.randomUUID().toString();
		default:
			break;
		}
		return null;
	}
}
