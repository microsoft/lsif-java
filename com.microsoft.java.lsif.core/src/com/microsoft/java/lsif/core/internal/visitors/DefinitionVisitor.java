/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class DefinitionVisitor extends ProtocolVisitor {

	public DefinitionVisitor(LsifService lsif, IndexerContext context) {
		super(lsif, context);
	}

	@Override
	public boolean visit(SimpleName node) {
		emitDefinition(node.getStartPosition(), node.getLength());
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		emitDefinition(node.getStartPosition(), node.getLength());
		return false;
	}

	private void emitDefinition(int startPosition, int length) {
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getCompilationUnit().getTypeRoot(),
					startPosition,
					length);

			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					fromRange.getStart().getLine(), fromRange.getStart().getCharacter(), new PreferenceManager(),
					new NullProgressMonitor());
			if (element == null) {
				return;
			}

			Location targetLocation = JdtlsUtils.getElementLocation(element);
			if (targetLocation == null) {
				return;
			}

			LsifService lsif = this.getLsif();
			Document docVertex = this.getContext().getDocVertex();

			// Source range:
			Range sourceRange = Repository.getInstance().enlistRange(lsif, docVertex, fromRange);

			// Target range:
			org.eclipse.lsp4j.Range toRange = targetLocation.getRange();
			Document targetDocument = Repository.getInstance().enlistDocument(lsif,
					targetLocation.getUri());
			Range targetRange = Repository.getInstance().enlistRange(lsif, targetDocument, toRange);

			// Result set
			ResultSet resultSet = Repository.getInstance().enlistResultSet(lsif, sourceRange);

			// Link resultSet & definitionResult
			DefinitionResult defResult = lsif.getVertexBuilder().definitionResult(targetRange.getId());
			LsifEmitter.getInstance().emit(defResult);
			LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().definition(resultSet, defResult));

		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping definition information ", ex);
		}
	}

}
