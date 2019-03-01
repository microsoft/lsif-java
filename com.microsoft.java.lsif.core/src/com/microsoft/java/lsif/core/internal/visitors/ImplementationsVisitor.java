/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.ImplementationsHandler;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class ImplementationsVisitor extends ProtocolVisitor {

	public ImplementationsVisitor() {
	}

	@Override
	public boolean visit(SimpleName node) {
		int startPosition = node.getStartPosition();
		int length = node.getLength();

		org.eclipse.lsp4j.Range fromRange;
		try {
			fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition, length);
		} catch (CoreException e) {
			return super.visit(node);
		}

		Emitter emitter = this.getContext().getEmitter();
		LsifService lsif = this.getContext().getLsif();

		List<Range> ranges = getImplementationRanges(fromRange.getStart().getLine(),
				fromRange.getStart().getCharacter());
		if (ranges == null || ranges.size() == 0) {
			return super.visit(node);
		}

		// Source range:
		Range sourceRange = this.enlistRange(this.getContext().getDocVertex(), fromRange);

		// Result set
		ResultSet resultSet = lsif.getVertexBuilder().resultSet();
		emitter.emit(resultSet);
		// From source range to ResultSet
		emitter.emit(lsif.getEdgeBuilder().refersTo(sourceRange, resultSet));

		// ImplementationResult
		List<Either<String, Location>> result = ranges.stream().map(r -> Either.<String, Location>forLeft(r.getId()))
				.collect(Collectors.toList());
		ImplementationResult implResult = lsif.getVertexBuilder().implementationResult(result);

		emitter.emit(implResult);
		emitter.emit(lsif.getEdgeBuilder().references(resultSet, implResult));
		return false;
	}

	private List<Range> getImplementationRanges(int line, int character) {
		List<? extends Location> locations = getImplementations(line, character);
		return locations.stream().map(loc -> this.enlistRange(loc.getUri(), loc.getRange())).filter(r -> r != null)
				.collect(Collectors.toList());
	}

	public List<? extends Location> getImplementations(int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(this.getContext().getDocVertex().getUri()), new Position(line, character));

		ImplementationsHandler proxy = new ImplementationsHandler(this.getContext().getPreferenceManger());
		return proxy.findImplementations(params, new NullProgressMonitor());
	}
}
