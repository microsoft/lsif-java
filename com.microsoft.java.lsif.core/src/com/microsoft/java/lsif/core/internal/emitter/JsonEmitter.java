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
