/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.HoverHandler;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.HoverResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class HoverVisitor extends ProtocolVisitor {

	public HoverVisitor() {
	}

	@Override
	public boolean visit(SimpleType type) {
		handleHover(type.getStartPosition(), type.getLength());
		return super.visit(type);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		handleHover(node.getStartPosition(), node.getLength());
		return super.visit(node);
	}

	private void handleHover(int startPosition, int length) {

		Emitter emitter = this.getContext().getEmitter();
		LsifService lsif = this.getContext().getLsif();
		Document docVertex = this.getContext().getDocVertex();
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition,
					length);

			// Source range:
			Range sourceRange = this.enlistRange(docVertex, fromRange);

			// Result set:
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			emitter.emit(resultSet);

			// From source range to ResultSet
			emitter.emit(lsif.getEdgeBuilder().refersTo(sourceRange, resultSet));

			// Link resultSet & definitionResult
			Hover result = hover(fromRange.getStart().getLine(), fromRange.getStart().getCharacter());
			HoverResult hoverResult = lsif.getVertexBuilder().hoverResult(result);
			emitter.emit(hoverResult);
			emitter.emit(lsif.getEdgeBuilder().hover(resultSet, hoverResult));

		} catch (CoreException e) {
			LanguageServerIndexerPlugin.log(e);
		}

	}

	private Hover hover(int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(this.getContext().getDocVertex().getUri()), new Position(line, character));

		HoverHandler proxy = new HoverHandler(this.getContext().getPreferenceManger());
		return proxy.hover(params, new NullProgressMonitor());
	}
}
