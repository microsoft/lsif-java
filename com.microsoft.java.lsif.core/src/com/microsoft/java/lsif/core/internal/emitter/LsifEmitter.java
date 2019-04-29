/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


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
