/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.emitter;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.protocol.Element;

public class JsonEmitter implements Emitter {

	private final Object lock = new Object();

	private boolean isFirst;

	public JsonEmitter() {
		this.isFirst = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#start()
	 */
	@Override
	public void start() {
		LanguageServerIndexerPlugin.println("[");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.microsoft.java.lsif.core.internal.emitter.Emitter#emit(com.microsoft.java
	 * .lsif.core.internal.protocol.Element)
	 */
	@Override
	public void emit(Element element) {
		String message = JsonParser.toJson(element);
		synchronized (lock) {
			if (!isFirst) {
				LanguageServerIndexerPlugin.println(",");
			}
			LanguageServerIndexerPlugin.print("\t");
			LanguageServerIndexerPlugin.print(message);
			this.isFirst = false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#end()
	 */
	@Override
	public void end() {
		if (!isFirst) {
			LanguageServerIndexerPlugin.println();
		}
		LanguageServerIndexerPlugin.println("]");
	}
}
