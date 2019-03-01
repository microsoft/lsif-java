/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Range;

public abstract class ProtocolVisitor extends ASTVisitor {

	private IndexerContext context;

	/**
	 * Constructor
	 */
	public ProtocolVisitor() {
	}

	public IndexerContext getContext() {
		return this.context;
	}

	public void setContext(IndexerContext context) {
		this.context = context;
	}

	public Document enlistDocument(String uri) {
		Repository repo = Repository.getInstance();
		uri = JdtlsUtils.normalizeUri(uri);
		Document targetDocument = repo.findDocumentByUri(uri);
		if (targetDocument == null) {
			targetDocument = this.context.getLsif().getVertexBuilder().document(uri);
			repo.addDocument(targetDocument);
			this.context.getEmitter().emit(targetDocument);
		}

		return targetDocument;
	}

	public Range enlistRange(Document docVertex, org.eclipse.lsp4j.Range lspRange) {
		Repository repo = Repository.getInstance();
		Range range = repo.findRange(docVertex.getUri(), lspRange);
		if (range == null) {
			range = this.context.getLsif().getVertexBuilder().range(lspRange);
			repo.addRange(docVertex, lspRange, range);
			this.context.getEmitter().emit(range);
			this.context.getEmitter().emit(this.context.getLsif().getEdgeBuilder().contains(docVertex, range));
		}
		return range;
	}

	public Range enlistRange(String uri, org.eclipse.lsp4j.Range lspRange) {
		return this.enlistRange(this.enlistDocument(uri), lspRange);
	}
}
