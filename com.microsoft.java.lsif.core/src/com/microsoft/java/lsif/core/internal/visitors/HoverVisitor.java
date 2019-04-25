/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.handlers.HoverHandler;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.HoverResult;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class HoverVisitor extends ProtocolVisitor {

	public HoverVisitor() {
	}

	@Override
	public boolean visit(SimpleType type) {
		emitHover(type.getStartPosition(), type.getLength());
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		emitHover(node.getStartPosition(), node.getLength());
		return false;
	}

	private void emitHover(int startPosition, int length) {
		LsifService lsif = this.getContext().getLsif();
		Document docVertex = this.getContext().getDocVertex();
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition,
					length);

			// Link resultSet & definitionResult
			Hover result = hover(fromRange.getStart().getLine(), fromRange.getStart().getCharacter());
			if (isEmpty(result)) {
				return;
			}

			// Source range:
			Range sourceRange = Repository.getInstance().enlistRange(this.getContext(), docVertex, fromRange);

			// Result set
			ResultSet resultSet = Repository.getInstance().enlistResultSet(this.getContext(), sourceRange);

			HoverResult hoverResult = Repository.getInstance().enlistHoverResult(this.getContext(), result);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().hover(resultSet, hoverResult));
		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping hover information ", ex);
		}
	}

	private Hover hover(int line, int character) {
		TextDocumentPositionParams params = new TextDocumentPositionParams(
				new TextDocumentIdentifier(this.getContext().getDocVertex().getUri()), new Position(line, character));

		HoverHandler proxy = new HoverHandler(this.getContext().getPreferenceManger());
		return proxy.hover(params, new NullProgressMonitor());
	}

	/**
	 * Check if the hover is empty or not.
	 *
	 * @param hover The {@link org.eclipse.lsp4j.Hover} returned from LSFJ.
	 * @return
	 */
	private boolean isEmpty(Hover hover) {
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
}
