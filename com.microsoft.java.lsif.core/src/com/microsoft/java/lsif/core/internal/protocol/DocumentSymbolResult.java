/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;

public class DocumentSymbolResult extends Vertex {

	// TODO: Support bag result.
	private List<DocumentSymbol> result;

	public DocumentSymbolResult(String id, List<DocumentSymbol> result) {
		super(id, Vertex.DOCUMENTSYMBOLRESULT);
		this.result = result;
	}

	public List<DocumentSymbol> getResult() {
		return this.result;
	}
}
