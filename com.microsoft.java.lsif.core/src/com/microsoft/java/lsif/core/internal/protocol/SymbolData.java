/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.visitors.VisitorUtils;

public class SymbolData {
	private ResultSet resultSet;
	private ReferenceResult referenceResult;
	private boolean definitionResolved;
	private boolean typeDefinitionResolved;
	private boolean implementationResolved;
	private boolean hoverResolved;

	synchronized public void ensureResultSet(LsifService lsif, Range sourceRange) {
		if (this.resultSet == null) {
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			LsifEmitter.getInstance().emit(resultSet);
			this.resultSet = resultSet;
		}
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().refersTo(sourceRange, this.resultSet));
	}

	synchronized public void resolveDefinition(LsifService lsif, Location definitionLocation) {
		if (this.definitionResolved) {
			return;
		}
		org.eclipse.lsp4j.Range definitionLspRange = definitionLocation.getRange();
		Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri());
		Range definitionRange = Repository.getInstance().enlistRange(lsif, definitionDocument, definitionLspRange);

		VisitorUtils.ensureDefinitionResult(lsif, this.resultSet, definitionRange);
		this.definitionResolved = true;
	}

	synchronized public void resolveTypeDefinition(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.typeDefinitionResolved) {
			return;
		}
		Location typeDefinitionLocation = VisitorUtils.resolveTypeDefinitionLocation(docVertex,
				sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (typeDefinitionLocation != null) {
			org.eclipse.lsp4j.Range typeDefinitionLspRange = typeDefinitionLocation.getRange();
			Document typeDefinitionDocument = Repository.getInstance().enlistDocument(lsif,
					typeDefinitionLocation.getUri());
			Range typeDefinitionRange = Repository.getInstance().enlistRange(lsif, typeDefinitionDocument,
					typeDefinitionLspRange);

			VisitorUtils.ensureTypeDefinitionResult(lsif, this.resultSet, typeDefinitionRange);
		}
		this.typeDefinitionResolved = true;
	}

	synchronized public void resolveImplementation(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.implementationResolved) {
			return;
		}
		List<Range> implementationRanges = VisitorUtils.getImplementationRanges(lsif, docVertex,
				sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (implementationRanges != null && implementationRanges.size() > 0) {

			// ImplementationResult
			List<Either<String, Location>> result = implementationRanges.stream()
					.map(r -> Either.<String, Location>forLeft(r.getId())).collect(Collectors.toList());
			VisitorUtils.ensureImplementationResult(lsif, this.resultSet, result);
		}
		this.implementationResolved = true;
	}

	synchronized public void resolveReference(LsifService lsif, Document sourceDocument, Location definitionLocation,
			Range sourceRange) {
		if (this.referenceResult == null) {
			ReferenceResult referenceResult = lsif.getVertexBuilder().referenceResult();
			LsifEmitter.getInstance().emit(referenceResult);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().references(this.resultSet, referenceResult));
			this.referenceResult = referenceResult;
		}

		Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri());
		Range definitionRange = Repository.getInstance().enlistRange(lsif, definitionDocument,
				definitionLocation.getRange());

		if (!VisitorUtils.isDefinitionItself(sourceDocument, sourceRange, definitionDocument, definitionRange)) {
			LsifEmitter.getInstance()
					.emit(lsif.getEdgeBuilder().referenceItem(this.referenceResult,
					sourceRange, ReferenceItem.REFERENCE));
		}
	}

	synchronized public void resolveHover(LsifService lsif, Document docVertex,
			org.eclipse.lsp4j.Range sourceLspRange) {
		if (this.hoverResolved) {
			return;
		}
		Hover hover = VisitorUtils.resolveHoverInformation(docVertex, sourceLspRange.getStart().getLine(),
				sourceLspRange.getStart().getCharacter());
		if (!VisitorUtils.isEmptyHover(hover)) {
			VisitorUtils.emitHoverResult(hover, lsif, this.resultSet);
		}
		this.hoverResolved = true;
	}
}
