/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public final class DocumentSymbolHandler {

	private DocumentSymbolHandler() {
	}

	public static List<DocumentSymbol> handle(String uri) {
		DocumentSymbolParams documentSymbolParams = new DocumentSymbolParams(new TextDocumentIdentifier(uri));
		org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler proxy = new org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler(true);
		List<Either<SymbolInformation, DocumentSymbol>> result = proxy.documentSymbol(documentSymbolParams, new NullProgressMonitor());

		return result.stream().map(either -> either.getRight()).collect(Collectors.toList());
	}
}
