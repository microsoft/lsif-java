/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.emitter;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.protocol.Element;

public class LineEmitter implements Emitter {

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
		LanguageServerIndexerPlugin.println(JsonParser.toJson(element));

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
