/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.emitter;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.java.lsif.core.internal.protocol.Element;

public class JsonEmitter implements Emitter {


	private List<Element> elements;


	private int counter = 0;

	public JsonEmitter() {
		elements = new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub

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
		this.elements.add(element);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.microsoft.java.lsif.core.internal.emitter.Emitter#end()
	 */
	@Override
	public void end() {
		// TODO Auto-generated method stub

	}

	public List<Element> getElements() {
		return this.elements;
	}



}
