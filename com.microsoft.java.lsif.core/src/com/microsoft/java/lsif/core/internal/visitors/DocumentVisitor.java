/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.DocumentSymbolResult;
import com.microsoft.java.lsif.core.internal.protocol.Project;

public class DocumentVisitor extends ProtocolVisitor {

	private Project projVertex;

	public DocumentVisitor(LsifService lsif, Project projVertex) {
		super(lsif, null /* DocumentVisitor does not need context information for parallel build */);
		this.projVertex = projVertex;
	}

	public Document enlist(IJavaElement sourceFile) {
		String uri = ResourceUtils.fixURI(sourceFile.getResource().getRawLocationURI());
		Document docVertex = Repository.getInstance().enlistDocument(this.getLsif(), uri, this.projVertex);

		handleDocumentSymbol(docVertex);
		return docVertex;
	}

	// TODO: Refine the symbol to range-based
	private void handleDocumentSymbol(Document docVertex) {
		List<DocumentSymbol> symbols = this.handle(docVertex.getUri());
		DocumentSymbolResult documentSymbolResult = this.getLsif().getVertexBuilder()
				.documentSymbolResult(symbols);
		LsifEmitter.getInstance().emit(documentSymbolResult);
		LsifEmitter.getInstance()
				.emit(this.getLsif().getEdgeBuilder().documentSymbols(docVertex, documentSymbolResult));
	}

	private List<DocumentSymbol> handle(String uri) {
		DocumentSymbolParams documentSymbolParams = new DocumentSymbolParams(new TextDocumentIdentifier(uri));
		DocumentSymbolHandler proxy = new DocumentSymbolHandler(true);
		List<Either<SymbolInformation, DocumentSymbol>> result = proxy.documentSymbol(documentSymbolParams,
				new NullProgressMonitor());

		return result.stream().map(either -> either.getRight()).collect(Collectors.toList());
	}
}
