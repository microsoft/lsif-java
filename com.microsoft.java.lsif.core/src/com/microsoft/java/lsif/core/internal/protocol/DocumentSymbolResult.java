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
