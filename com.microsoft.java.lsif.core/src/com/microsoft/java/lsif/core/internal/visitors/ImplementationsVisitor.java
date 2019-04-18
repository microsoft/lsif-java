/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.ImplementationsHandler;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class ImplementationsVisitor extends ProtocolVisitor {

	public ImplementationsVisitor() {
	}

	@Override
	public boolean visit(SimpleName node) {
		if (node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration) {
			this.emitImplementation(node.getStartPosition(), node.getLength());
		}
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		if (node.getParent() instanceof TypeDeclaration) {
			this.emitImplementation(node.getStartPosition(), node.getLength());
		}
		return false;
	}

	private void emitImplementation(int startPosition, int length) {
		org.eclipse.lsp4j.Range fromRange;
		try {
			fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition, length);

			Emitter emitter = this.getContext().getEmitter();
			LsifService lsif = this.getContext().getLsif();

			List<Range> ranges = getImplementationRanges(fromRange.getStart().getLine(),
					fromRange.getStart().getCharacter());
			if (ranges == null || ranges.size() == 0) {
				return;
			}

			// Source range:
			Range sourceRange = Repository.getInstance().enlistRange(this.getContext(),
					this.getContext().getDocVertex(), fromRange);

			// Result set
			ResultSet resultSet = Repository.getInstance().enlistResultSet(this.getContext(), sourceRange);

			// ImplementationResult
			List<Either<String, Location>> result = ranges.stream()
					.map(r -> Either.<String, Location>forLeft(r.getId())).collect(Collectors.toList());
			ImplementationResult implResult = lsif.getVertexBuilder().implementationResult(result);

			emitter.emit(implResult);
			emitter.emit(lsif.getEdgeBuilder().implementation(resultSet, implResult));

		} catch (CoreException ex) {
			LanguageServerIndexerPlugin.logException("Exception when visiting implementation ", ex);
		}
	}

	private List<Range> getImplementationRanges(int line, int character) {
		List<? extends Location> locations = getImplementations(line, character);
		if (locations == null) {
			return Collections.emptyList();
		}
		return locations.stream()
				.map(loc -> Repository.getInstance().enlistRange(this.getContext(), loc.getUri(), loc.getRange()))
				.filter(r -> r != null)
				.collect(Collectors.toList());
	}

	public List<? extends Location> getImplementations(int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(this.getContext().getDocVertex().getUri()), new Position(line, character));

		ImplementationsHandler proxy = new ImplementationsHandler(this.getContext().getPreferenceManger());
		return proxy.findImplementations(params, new NullProgressMonitor());
	}
}
