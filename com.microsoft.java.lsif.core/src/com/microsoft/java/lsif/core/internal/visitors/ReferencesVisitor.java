/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceItem;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceResult;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class ReferencesVisitor extends ProtocolVisitor {

	public ReferencesVisitor() {
	}

	@Override
	public boolean visit(SimpleType type) {
		emitReferences(type.getStartPosition(), type.getLength());
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		emitReferences(node.getStartPosition(), node.getLength());
		return false;
	}

	public void emitReferences(int startPosition, int length) {

		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition,
					length);

			if (fromRange == null) {
				return;
			}

			LsifService lsif = this.getContext().getLsif();

			List<Range> ranges = getReferenceRanges(fromRange.getStart().getLine(),
					fromRange.getStart().getCharacter());
			if (ranges == null || ranges.size() == 0) {
				return;
			}

			// Source range:
			Range sourceRange = Repository.getInstance().enlistRange(this.getContext(),
					this.getContext().getDocVertex(), fromRange);

			// Result set
			ResultSet resultSet = Repository.getInstance().enlistResultSet(this.getContext(), sourceRange);

			// ReferenceResult
			ReferenceResult refResult = lsif.getVertexBuilder().referenceResult();
			LsifEmitter.getInstance().emit(refResult);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().references(resultSet, refResult));

			for (Range r : ranges) {
				LsifEmitter.getInstance()
						.emit(lsif.getEdgeBuilder().referenceItem(refResult, r, ReferenceItem.REFERENCE));
			}
		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping reference information ", ex);
		}
	}

	private List<Range> getReferenceRanges(int line, int character) {
		List<Location> locations = getReferenceLocations(line, character);
		return locations.stream()
				.map(loc -> Repository.getInstance().enlistRange(this.getContext(), loc.getUri(), loc.getRange()))
				.filter(r -> r != null)
				.collect(Collectors.toList());
	}

	private List<Location> getReferenceLocations(int line, int character) {
		ReferenceParams params = new ReferenceParams(new ReferenceContext(true));
		params.setTextDocument(new TextDocumentIdentifier(this.getContext().getDocVertex().getUri()));
		params.setPosition(new Position(line, character));

		org.eclipse.jdt.ls.core.internal.handlers.ReferencesHandler proxy = new org.eclipse.jdt.ls.core.internal.handlers.ReferencesHandler(
				this.getContext().getPreferenceManger());
		List<Location> references = proxy.findReferences(params, new NullProgressMonitor());
		return references;
	}

}
