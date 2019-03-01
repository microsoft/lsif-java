/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.TypeDefinitionResult;

public class TypeDefinitionVisitor extends ProtocolVisitor {

	public TypeDefinitionVisitor() {
	}

	public void handle(SingleVariableDeclaration node) {
		Type declarationType = node.getType();
		handleTypeDefinition(declarationType.getStartPosition(), declarationType.getLength());
	}

	public void handle(SimpleType node) {
		handleTypeDefinition(node.getStartPosition(), node.getLength());
	}

	private void handleTypeDefinition(int startPosition, int length) {
		Emitter emitter = this.getContext().getEmitter();
		LsifService lsif = this.getContext().getLsif();
		Document docVertex = this.getContext().getDocVertex();
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition,
					length);

			Location targetLocation = computeTypeDefinitionNavigation(docVertex.getUri(),
					fromRange.getStart().getLine(), fromRange.getStart().getCharacter());

			if (targetLocation == null) {
				return;
			}

			// Definition start position
			// Source range:
			Range sourceRange = this.enlistRange(docVertex, fromRange);

			// Target range:
			org.eclipse.lsp4j.Range toRange = targetLocation.getRange();
			Document targetDocument = this.enlistDocument(targetLocation.getUri());
			Range targetRange = this.enlistRange(targetDocument, toRange);

			// Result set
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			emitter.emit(resultSet);

			// From source range to ResultSet
			emitter.emit(lsif.getEdgeBuilder().refersTo(sourceRange, resultSet));

			// Link resultSet & typeDefinitionResult
			TypeDefinitionResult defResult = lsif.getVertexBuilder().typeDefinitionResult(targetRange.getId());
			emitter.emit(defResult);
			emitter.emit(lsif.getEdgeBuilder().typeDefinition(resultSet, defResult));
		} catch (CoreException e) {
			LanguageServerIndexerPlugin.log(e);
		}
	}

	private static Location computeTypeDefinitionNavigation(String uri, int line, int column) {
		TextDocumentPositionParams documentSymbolParams = new TextDocumentPositionParams(
				new TextDocumentIdentifier(uri), new Position(line, column));
		org.eclipse.jdt.ls.core.internal.handlers.NavigateToTypeDefinitionHandler proxy = new org.eclipse.jdt.ls.core.internal.handlers.NavigateToTypeDefinitionHandler();
		List<? extends Location> typeDefinition = proxy.typeDefinition(documentSymbolParams, new NullProgressMonitor());
		return typeDefinition.size() > 0 ? typeDefinition.get(0) : null;
	}
}
