/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LsifUtils;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.DiagnosticResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.DocumentSymbolResult;
import com.microsoft.java.lsif.core.internal.protocol.Event;
import com.microsoft.java.lsif.core.internal.protocol.HoverResult;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.MetaData;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceResult;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.TypeDefinitionResult;

public final class VertexBuilder {

	private IdGenerator generator;

	public VertexBuilder(IdGenerator generator) {
		this.generator = generator;
	}

	public MetaData metaData(String projectRoot) {
		return new MetaData(generator.next(), projectRoot);
	}

	public Event event(String scope, String kind) {
		return new Event(generator.next(), scope, kind);
	}

	public Project project() {
		return new Project(generator.next());
	}

	public Document document(String uri) {
		uri = LsifUtils.normalizeUri(uri);
		Document res = new Document(generator.next(), uri);
		String base64Contents = LsifUtils.encodeToBase64(JdtlsUtils.getDocumentContent(uri));
		res.setContents(base64Contents);
		return res;
	}

	public Range range(org.eclipse.lsp4j.Range lspRange) {
		return new Range(generator.next(), lspRange.getStart(), lspRange.getEnd());
	}

	public ResultSet resultSet() {
		return new ResultSet(generator.next());
	}

	public DefinitionResult definitionResult(String resultId) {
		return new DefinitionResult(generator.next(), resultId);
	}

	public HoverResult hoverResult(Hover hover) {
		return new HoverResult(generator.next(), hover);
	}

	public TypeDefinitionResult typeDefinitionResult(String resultId) {
		return new TypeDefinitionResult(generator.next(), resultId);
	}

	public ReferenceResult referenceResult() {
		return new ReferenceResult(generator.next());
	}

	public ReferenceResult referenceResult(List<String> declarations, List<String> definitions, List<String> references,
			List<String> referenceResults) {
		return new ReferenceResult(generator.next(), declarations, definitions, references, referenceResults);
	}

	public ImplementationResult implementationResult(List<Either<String, Location>> result) {
		return new ImplementationResult(generator.next(), result);
	}

	public DocumentSymbolResult documentSymbolResult(List<DocumentSymbol> symbols) {
		return new DocumentSymbolResult(generator.next(), symbols);
	}

	public DiagnosticResult diagnosticResult(List<Diagnostic> diagnostics) {
		return new DiagnosticResult(generator.next(), diagnostics);
	}
}
