/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import org.eclipse.lsp4j.Hover;

public class HoverResult extends Vertex {

	public Hover result;

	public HoverResult(String id, Hover result) {
		super(id, Vertex.HOVERRESULT);
		this.result = result;
	}

	public Hover getResult() {
		return this.result;
	}
}
