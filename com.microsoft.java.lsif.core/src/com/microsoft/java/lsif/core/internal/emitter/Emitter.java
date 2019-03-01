/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.emitter;

import java.util.List;

import com.microsoft.java.lsif.core.internal.protocol.Element;

public interface Emitter {

	void start();

	void emit(Element element);

	void end();

	List<Element> getElements();
}
