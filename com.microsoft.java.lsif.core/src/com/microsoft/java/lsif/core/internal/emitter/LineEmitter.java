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

public class LineEmitter implements Emitter {

	private final Object lock = new Object();

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#start()
	 */
	@Override
	public void start() {

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
			LanguageServerIndexerPlugin.println(message);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#end()
	 */
	@Override
	public void end() {

	}

}
