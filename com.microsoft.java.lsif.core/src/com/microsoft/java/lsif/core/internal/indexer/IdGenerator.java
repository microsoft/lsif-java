/*******************************************************************************
* Copyright (c) 2021 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

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
				return String.valueOf(counter.incrementAndGet());
			case UUID:
				return UUID.randomUUID().toString();
			default:
				break;
		}
		return null;
	}
}
