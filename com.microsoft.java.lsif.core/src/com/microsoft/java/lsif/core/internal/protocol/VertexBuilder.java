/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;

public final class VertexBuilder {

	private IdGenerator generator;

	private Map<String, Document> documentMap = new ConcurrentHashMap<>();

	private Map<String, Map<org.eclipse.lsp4j.Range, Range>> rangeMap = new ConcurrentHashMap<>();

	public VertexBuilder(IdGenerator generator) {
		this.generator = generator;
	}

	public MetaData metaData(String version) {
		return new MetaData(generator.next(), version);
	}

	public Project project() {
		return new Project(generator.next());
	}

	public Document document(String uri) {
		uri = JdtlsUtils.normalizeUri(uri);
		Document res = new Document(generator.next(), uri);
		documentMap.put(uri, res);
		return res;
	}

	public Range range(String documentId, org.eclipse.lsp4j.Range lspRange) {
		Map<org.eclipse.lsp4j.Range, Range> rangeSet = this.rangeMap.getOrDefault(documentId, new HashMap<>());
		Range range = new Range(generator.next(), lspRange.getStart(), lspRange.getEnd());
		rangeSet.put(lspRange, range);
		rangeMap.put(documentId, rangeSet);
		return range;
	}

	public Range range(String documentId, org.eclipse.lsp4j.Range lspRange, Tag tag) {
		Map<org.eclipse.lsp4j.Range, Range> rangeSet = this.rangeMap.getOrDefault(documentId, new HashMap<>());
		Range range = new Range(generator.next(), lspRange.getStart(), lspRange.getEnd(), tag);
		rangeSet.put(lspRange, range);
		rangeMap.put(documentId, rangeSet);
		return range;
	}

	public ResultSet resultSet() {
		return new ResultSet(generator.next());
	}

	public DefinitionResult definitionResult(String resultId) {
		return new DefinitionResult(generator.next(), resultId);
	}

	public Document getDocument(String uri) {
		uri = JdtlsUtils.normalizeUri(uri);
		return documentMap.getOrDefault(uri, null);
	}

	public Range getRange(String documentId, org.eclipse.lsp4j.Range lspRange) {
		Map<org.eclipse.lsp4j.Range, Range> rangeSet = rangeMap.getOrDefault(documentId, null);
		if (rangeSet != null) {
			return rangeSet.getOrDefault(lspRange, null);
		}
		return null;
	}
}
