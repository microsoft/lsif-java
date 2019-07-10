/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.visitors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.handlers.HoverHandler;
import org.eclipse.jdt.ls.core.internal.handlers.ImplementationsHandler;
import org.eclipse.jdt.ls.core.internal.handlers.NavigateToTypeDefinitionHandler;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.HoverResult;
import com.microsoft.java.lsif.core.internal.protocol.ImplementationResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.TypeDefinitionResult;

public class VisitorUtils {

	/* resultSet */
	public static ResultSet ensureResultSet(LsifService lsif, Range sourceRange) {
		ResultSet resultSet = lsif.getVertexBuilder().resultSet();
		LsifEmitter.getInstance().emit(resultSet);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().refersTo(sourceRange, resultSet));
		return resultSet;
	}

	/* definition */
	public static void ensureDefinitionResult(LsifService lsif, ResultSet resultSet, Range targetRange) {
		DefinitionResult defResult = lsif.getVertexBuilder().definitionResult(targetRange.getId());
		LsifEmitter.getInstance().emit(defResult);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().definition(resultSet, defResult));
	}

	/* typeDefinition */
	public static Location resolveTypeDefinitionLocation(Document docVertex, int line, int column) {
		TextDocumentPositionParams documentSymbolParams = new TextDocumentPositionParams(
				new TextDocumentIdentifier(docVertex.getUri()), new Position(line, column));
		NavigateToTypeDefinitionHandler proxy = new NavigateToTypeDefinitionHandler();
		List<? extends Location> typeDefinition = proxy.typeDefinition(documentSymbolParams, new NullProgressMonitor());
		return typeDefinition != null && typeDefinition.size() > 0 ? typeDefinition.get(0) : null;
	}

	public static void ensureTypeDefinitionResult(LsifService lsif, ResultSet resultSet, Range targetRange) {
		TypeDefinitionResult typeDefinitionResult = lsif.getVertexBuilder().typeDefinitionResult(targetRange.getId());
		LsifEmitter.getInstance().emit(typeDefinitionResult);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().typeDefinition(resultSet, typeDefinitionResult));
	}

	/* implementation */
	public static List<Range> getImplementationRanges(LsifService lsif, Document docVertex, int line, int character) {
		List<? extends Location> locations = getImplementations(docVertex, line, character);
		if (locations == null) {
			return Collections.emptyList();
		}
		return locations.stream().map(loc -> Repository.getInstance().enlistRange(lsif, loc.getUri(), loc.getRange()))
				.filter(r -> r != null).collect(Collectors.toList());
	}

	public static List<? extends Location> getImplementations(Document docVertex, int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(docVertex.getUri()), new Position(line, character));

		ImplementationsHandler proxy = new ImplementationsHandler(JavaLanguageServerPlugin.getPreferencesManager());
		return proxy.findImplementations(params, new NullProgressMonitor());
	}

	public static void ensureImplementationResult(LsifService lsif, ResultSet resultSet,
			List<Either<String, Location>> result) {
		ImplementationResult implResult = lsif.getVertexBuilder().implementationResult(result);
		LsifEmitter.getInstance().emit(implResult);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().implementation(resultSet, implResult));
	}

	/* reference */
	public static boolean isDefinitionItself(Document sourceDoc, Range sourceRange, Document definitionDoc,
			Range definitionRange) {
		return JdtlsUtils.normalizeUri(definitionDoc.getUri()).equals(JdtlsUtils.normalizeUri(sourceDoc.getUri()))
				&& sourceRange.equals(definitionRange);
	}

	/* hover */
	public static Hover resolveHoverInformation(Document docVertex, int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(docVertex.getUri()), new Position(line, character));

		HoverHandler proxy = new HoverHandler(JavaLanguageServerPlugin.getPreferencesManager());
		return proxy.hover(params, new NullProgressMonitor());
	}

	public static boolean isEmptyHover(Hover hover) {
		Either<List<Either<String, MarkedString>>, MarkupContent> content = hover.getContents();
		if (content == null) {
			return true;
		}

		if (content.isRight()) {
			return false;
		}

		List<Either<String, MarkedString>> list = content.getLeft();
		if (list == null || list.size() == 0) {
			return true;
		}

		for (Either<String, MarkedString> either : list) {
			if (StringUtils.isNotEmpty(either.getLeft()) || either.isRight()) {
				return false;
			}
		}

		return true;
	}

	public static void emitHoverResult(Hover hover, LsifService lsif, ResultSet resultSet) {
		HoverResult hoverResult = lsif.getVertexBuilder().hoverResult(hover);
		LsifEmitter.getInstance().emit(hoverResult);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().hover(resultSet, hoverResult));
	}
}
