/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.lsp4j.Hover;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.HoverResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class Repository {

	// Key: document URI
	// Value: Document object
	private Map<String, Document> documentMap = new ConcurrentHashMap<>();

	// Key: document URI
	// Value: ranges among the documents
	// Key: LSP range
	// LSIF: range
	private Map<String, Map<org.eclipse.lsp4j.Range, Range>> rangeMap = new ConcurrentHashMap<>();

	// Key: Range
	// Value: ResultSet that range refers to
	private Map<Range, ResultSet> resultSetMap = new ConcurrentHashMap<>();

	// Key: Hash Code of the Hover Content
	// Value: HoverResult
	private Map<Integer, HoverResult> hoverResultMap = new ConcurrentHashMap<>();

	private Repository() {
	}

	private static class RepositoryHolder {
		private static final Repository INSTANCE = new Repository();
	}

	public static Repository getInstance() {
		return RepositoryHolder.INSTANCE;
	}

	public synchronized Document enlistDocument(IndexerContext context, String uri) {
		uri = JdtlsUtils.normalizeUri(uri);
		Document targetDocument = findDocumentByUri(uri);
		if (targetDocument == null) {
			targetDocument = context.getLsif().getVertexBuilder().document(uri);
			addDocument(targetDocument);
			LsifEmitter.getInstance().emit(targetDocument);
		}

		return targetDocument;
	}

	public synchronized ResultSet enlistResultSet(IndexerContext context, Range range) {
		ResultSet resultSet = findResultSetByRange(range);
		if (resultSet == null) {
			resultSet = context.getLsif().getVertexBuilder().resultSet();
			addResultSet(range, resultSet);
			LsifEmitter.getInstance().emit(resultSet);
			LsifEmitter.getInstance().emit(context.getLsif().getEdgeBuilder().refersTo(range, resultSet));
		}

		return resultSet;
	}

	public synchronized Range enlistRange(IndexerContext context, Document docVertex,
			org.eclipse.lsp4j.Range lspRange) {
		Range range = findRange(docVertex.getUri(), lspRange);
		if (range == null) {
			range = context.getLsif().getVertexBuilder().range(lspRange);
			addRange(docVertex, lspRange, range);
			LsifEmitter.getInstance().emit(range);
			LsifEmitter.getInstance().emit(context.getLsif().getEdgeBuilder().contains(docVertex, range));
		}
		return range;
	}

	public synchronized HoverResult enlistHoverResult(IndexerContext context, Hover hover) {
		int contentHash = hover.getContents().hashCode();
		HoverResult hoverResult = findHoverResultByHashCode(contentHash);
		if (hoverResult == null) {
			hoverResult = context.getLsif().getVertexBuilder().hoverResult(hover);
			LsifEmitter.getInstance().emit(hoverResult);
			addHoverResult(contentHash, hoverResult);
		}
		return hoverResult;
	}

	public Range enlistRange(IndexerContext context, String uri, org.eclipse.lsp4j.Range lspRange) {
		return enlistRange(context, enlistDocument(context, uri), lspRange);
	}

	private void addDocument(Document doc) {
		this.documentMap.put(doc.getUri(), doc);
	}

	private void addRange(Document owner, org.eclipse.lsp4j.Range lspRange, Range range) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = this.rangeMap.computeIfAbsent(owner.getUri(),
				s -> new ConcurrentHashMap<>());
		ranges.putIfAbsent(lspRange, range);
	}

	private void addResultSet(Range range, ResultSet resultSet) {
		this.resultSetMap.put(range, resultSet);
	}

	private void addHoverResult(int hashCode, HoverResult hoverResult) {
		this.hoverResultMap.put(hashCode, hoverResult);
	}

	private Document findDocumentByUri(String uri) {
		return this.documentMap.getOrDefault(uri, null);
	}

	private Range findRange(String uri, org.eclipse.lsp4j.Range lspRange) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = rangeMap.get(uri);
		if (ranges != null) {
			return ranges.get(lspRange);
		}
		return null;
	}

	private ResultSet findResultSetByRange(Range range) {
		return this.resultSetMap.getOrDefault(range, null);
	}

	public HoverResult findHoverResultByHashCode(int hashCode) {
		return this.hoverResultMap.getOrDefault(hashCode, null);
	}
}
