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


package com.microsoft.java.lsif.core.internal.emitter;

public class LsifEmitter {

	private LsifEmitter() {
	}

	private static class LsifEmitterHolder {
		private static final Emitter INSTANCE = createEmitter();

		private static Emitter createEmitter() {
			final String format = System.getProperty("output.format", "line" /* default */);
			switch (format) {
				case "json":
					return new JsonEmitter();
				case "line":
					return new LineEmitter();
				default:
					throw new RuntimeException("Unsupported output format: " + format);
			}
		}
	}

	public static Emitter getInstance() {
		return LsifEmitterHolder.INSTANCE;
	}
}
