/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceItem;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceResult;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class ReferencesVisitor extends ProtocolVisitor {

	public ReferencesVisitor(IndexerContext context) {
		super(context);
	}

	public void handle(MethodDeclaration declaration) {

		int startPosition = declaration.getStartPosition();
		int length = declaration.getLength();

		org.eclipse.lsp4j.Range fromRange;
		try {
			fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition, length);
		} catch (CoreException e) {
			return;
		}

		Emitter emitter = this.getContext().getEmitter();
		LsifService lsif = this.getContext().getLsif();

		// Source range:
		Range sourceRange = this.enlistRange(this.getContext().getDocVertex(), fromRange);

		// Result set
		ResultSet resultSet = lsif.getVertexBuilder().resultSet();
		emitter.emit(resultSet);
		// From source range to ResultSet
		emitter.emit(lsif.getEdgeBuilder().refersTo(sourceRange, resultSet));

		// ReferenceResult
		ReferenceResult refResult = lsif.getVertexBuilder().referenceResult();
		emitter.emit(refResult);
		emitter.emit(lsif.getEdgeBuilder().references(resultSet, refResult));

		List<Range> referenceRangeIds = getReferenceRanges(this.getContext().getDocVertex().getUri(), fromRange.getStart().getLine(),
				fromRange.getStart().getCharacter());

		for (Range r : referenceRangeIds) {
			emitter.emit(lsif.getEdgeBuilder().referenceItem(refResult, r, ReferenceItem.REFERENCE));
		}
	}

	private List<Range> getReferenceRanges(String uri, int line, int character) {
		List<Location> locations = handle(uri, line, character);
		return locations.stream().map(loc -> this.enlistRange(loc.getUri(), loc.getRange())).filter(r -> r != null).collect(Collectors.toList());
	}

	private List<Location> handle(String uri, int line, int character) {
		ReferenceParams params = new ReferenceParams(new ReferenceContext(true));
		params.setTextDocument(new TextDocumentIdentifier(uri));
		params.setPosition(new Position(line, character));

		org.eclipse.jdt.ls.core.internal.handlers.ReferencesHandler proxy = new org.eclipse.jdt.ls.core.internal.handlers.ReferencesHandler(
				this.getContext().getPreferenceManger());
		List<Location> references = proxy.findReferences(params, new NullProgressMonitor());
		return references;
	}

}
