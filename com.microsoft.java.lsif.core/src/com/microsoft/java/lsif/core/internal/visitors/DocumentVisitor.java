/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.lsp4j.DocumentSymbol;

import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.DocumentSymbolResult;
import com.microsoft.java.lsif.core.internal.protocol.Project;

public class DocumentVisitor extends ProtocolVisitor {

	private Project projVertex;

	public DocumentVisitor(IndexerContext context, Project projVertex) {
		super(context);
		this.projVertex = projVertex;
	}

	public Document enlist(IJavaElement sourceFile) {
		String uri = ResourceUtils.fixURI(sourceFile.getResource().getRawLocationURI());
		Document docVertex = this.enlistDocument(uri);
		this.getContext().getEmitter().emit(this.getContext().getLsif().getEdgeBuilder().contains(projVertex, docVertex));

		handleDocumentSymbol(docVertex);
		return docVertex;
	}

	// TODO: Refine the symbol to range-based
	private void handleDocumentSymbol(Document docVertex) {
		List<DocumentSymbol> symbols = DocumentSymbolHandler.handle(docVertex.getUri());
		DocumentSymbolResult documentSymbolResult = this.getContext().getLsif().getVertexBuilder().documentSymbolResult(symbols);
		this.getContext().getEmitter().emit(documentSymbolResult);
		this.getContext().getEmitter().emit(this.getContext().getLsif().getEdgeBuilder().documentSymbols(docVertex, documentSymbolResult));
	}
}
