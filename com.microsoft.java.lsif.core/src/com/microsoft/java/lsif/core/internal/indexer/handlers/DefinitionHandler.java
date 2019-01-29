/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentSymbolHandler;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionTag;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Edge;
import com.microsoft.java.lsif.core.internal.protocol.JavaLsif;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.Vertex;

public class DefinitionHandler {

	public static void handler(SingleVariableDeclaration node, IndexerContext context) {
		Type declarationType = node.getType();

		Emitter emitter = context.getEmitter();
		JavaLsif lsif = context.getLsif();
		Document docVertex = context.getDocVertex();
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(context.getTypeRoot(), declarationType.getStartPosition(), declarationType.getLength());

			IJavaElement element = JDTUtils.findElementAtSelection(context.getTypeRoot(), fromRange.getStart().getLine(), fromRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
			if (element == null) {
				return;
			}

			Location targetLocation = null;
			ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
			IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
			if (compilationUnit != null || (cf != null && cf.getSourceRange() != null)) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(element), context.getTypeRoot().getJavaProject());
			}
			if (element instanceof IMember && ((IMember) element).getClassFile() != null) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(((IMember) element).getClassFile()),
						context.getTypeRoot().getJavaProject());
			}

			if (targetLocation == null) {
				return;
			}

			// Definition start position
			Vertex from = lsif.getVertexBuilder().range(docVertex.getId(), fromRange);
			emitter.emit(from);
			emitter.emit(lsif.getEdgeBuilder().contains(docVertex, from));

			// Result set
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			emitter.emit(resultSet);
			Edge fromToResultSet = lsif.getEdgeBuilder().refersTo(from, resultSet);

			org.eclipse.lsp4j.Range targetRange = targetLocation.getRange();

			Document targetDocument = lsif.getVertexBuilder().getDocument(targetLocation.getUri());
			if (targetDocument == null) {
				targetDocument = lsif.getVertexBuilder().document(targetLocation.getUri());
				emitter.emit(targetDocument);
			}

			// Definition range
			Range to = lsif.getVertexBuilder().getRange(targetDocument.getId(), targetRange);
			if (to == null) {
				DefinitionTag tag = new DefinitionTag(element.getElementName(), DocumentSymbolHandler.mapKind(element), false, null, null);
				to = lsif.getVertexBuilder().range(targetDocument.getId(), targetRange, tag);
				emitter.emit(to);
				emitter.emit(lsif.getEdgeBuilder().contains(targetDocument, to));
			}

			// Link resultSet & definitionResult
			DefinitionResult defResult = lsif.getVertexBuilder().definitionResult(to.getId());
			emitter.emit(defResult);
			emitter.emit(lsif.getEdgeBuilder().definition(resultSet, defResult));

			// From source range to ResultSet
			emitter.emit(lsif.getEdgeBuilder().refersTo(from, resultSet));

		} catch (CoreException e) {
			LanguageServerIndexerPlugin.log(e);
		}

	}

	public static void handler(SimpleType node, IndexerContext context) {

		Emitter emitter = context.getEmitter();
		JavaLsif lsif = context.getLsif();
		Document docVertex = context.getDocVertex();
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(context.getTypeRoot(), node.getStartPosition(), node.getLength());

			IJavaElement element = JDTUtils.findElementAtSelection(context.getTypeRoot(), fromRange.getStart().getLine(), fromRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
			if (element == null) {
				return;
			}

			Location targetLocation = null;
			ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
			IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
			if (compilationUnit != null || (cf != null && cf.getSourceRange() != null)) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(element), context.getTypeRoot().getJavaProject());
			}
			if (element instanceof IMember && ((IMember) element).getClassFile() != null) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(((IMember) element).getClassFile()),
						context.getTypeRoot().getJavaProject());
			}

			if (targetLocation == null) {
				return;
			}

			// Definition start position
			Vertex from = lsif.getVertexBuilder().range(docVertex.getId(), fromRange);
			emitter.emit(from);
			emitter.emit(lsif.getEdgeBuilder().contains(docVertex, from));

			// Result set
			ResultSet resultSet = lsif.getVertexBuilder().resultSet();
			emitter.emit(resultSet);
			Edge fromToResultSet = lsif.getEdgeBuilder().refersTo(from, resultSet);

			org.eclipse.lsp4j.Range targetRange = targetLocation.getRange();

			Document targetDocument = lsif.getVertexBuilder().getDocument(targetLocation.getUri());
			if (targetDocument == null) {
				targetDocument = lsif.getVertexBuilder().document(targetLocation.getUri());
				emitter.emit(targetDocument);
			}

			// Definition range
			Range to = lsif.getVertexBuilder().getRange(targetDocument.getId(), targetRange);
			if (to == null) {
				DefinitionTag tag = new DefinitionTag(element.getElementName(), DocumentSymbolHandler.mapKind(element), false, null, null);
				to = lsif.getVertexBuilder().range(targetDocument.getId(), targetRange, tag);
				emitter.emit(to);
				emitter.emit(lsif.getEdgeBuilder().contains(targetDocument, to));
			}

			// Link resultSet & definitionResult
			DefinitionResult defResult = lsif.getVertexBuilder().definitionResult(to.getId());
			emitter.emit(defResult);
			emitter.emit(lsif.getEdgeBuilder().definition(resultSet, defResult));

			// From source range to ResultSet
			emitter.emit(lsif.getEdgeBuilder().refersTo(from, resultSet));

		} catch (CoreException e) {
			LanguageServerIndexerPlugin.log(e);
		}
	}
}
