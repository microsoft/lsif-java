/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	private static Repository instance = new Repository();

	private Repository() {
	}

	public static Repository getInstance() {
		return instance;
	}

	public void addDocument(Document doc) {
		this.documentMap.put(doc.getUri(), doc);
	}

	public void addRange(Document owner, org.eclipse.lsp4j.Range lspRange, Range range) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = this.rangeMap.computeIfAbsent(owner.getUri(),
				s -> new ConcurrentHashMap<>());
		ranges.putIfAbsent(lspRange, range);
	}

	public void addResultSet(Range range, ResultSet resultSet) {
		this.resultSetMap.put(range, resultSet);
	}

	public void addHoverResult(int hashCode, HoverResult hoverResult) {
		this.hoverResultMap.put(hashCode, hoverResult);
	}

	public Document findDocumentByUri(String uri) {
		return this.documentMap.getOrDefault(uri, null);
	}

	public Document findDocumentById() {
		throw new UnsupportedOperationException();
	}

	public Range findRange(String uri, org.eclipse.lsp4j.Range lspRange) {
		Map<org.eclipse.lsp4j.Range, Range> ranges = rangeMap.get(uri);
		if (ranges != null) {
			return ranges.get(lspRange);
		}
		return null;
	}

	public ResultSet findResultSetByRange(Range range) {
		return this.resultSetMap.getOrDefault(range, null);
	}

	public HoverResult findHoverResultByHashCode(int hashCode) {
		return this.hoverResultMap.getOrDefault(hashCode, null);
	}
}
