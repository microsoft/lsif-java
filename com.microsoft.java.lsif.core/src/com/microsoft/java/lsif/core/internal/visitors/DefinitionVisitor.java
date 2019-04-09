/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.DefinitionResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class DefinitionVisitor extends ProtocolVisitor {

	public DefinitionVisitor() {
	}

	@Override
	public boolean visit(SimpleName node) {
		emitDefinition(node.getStartPosition(), node.getLength());
		return false;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		Type declarationType = node.getType();
		emitDefinition(declarationType.getStartPosition(), declarationType.getLength());
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		emitDefinition(node.getStartPosition(), node.getLength());
		return false;
	}

	private void emitDefinition(int startPosition, int length) {
		try {
			org.eclipse.lsp4j.Range fromRange = JDTUtils.toRange(this.getContext().getTypeRoot(), startPosition,
					length);

			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getTypeRoot(),
					fromRange.getStart().getLine(), fromRange.getStart().getCharacter(), new PreferenceManager(),
					new NullProgressMonitor());
			if (element == null) {
				return;
			}

			Location targetLocation = JdtlsUtils.getElementLocation(element);
			if (targetLocation == null) {
				return;
			}

			Emitter emitter = this.getContext().getEmitter();
			LsifService lsif = this.getContext().getLsif();
			Document docVertex = this.getContext().getDocVertex();

			// Source range:
			Range sourceRange = this.enlistRange(docVertex, fromRange);

			// Target range:
			org.eclipse.lsp4j.Range toRange = targetLocation.getRange();
			Document targetDocument = this.enlistDocument(targetLocation.getUri());
			Range targetRange = this.enlistRange(targetDocument, toRange);

			// Result set
			ResultSet resultSet = this.enlistResultSet(sourceRange);

			// Link resultSet & definitionResult
			DefinitionResult defResult = lsif.getVertexBuilder().definitionResult(targetRange.getId());
			emitter.emit(defResult);
			emitter.emit(lsif.getEdgeBuilder().definition(resultSet, defResult));

		} catch (CoreException ex) {
			LanguageServerIndexerPlugin.logException("Exception in definition visitor: ", ex);
		}
	}

}
