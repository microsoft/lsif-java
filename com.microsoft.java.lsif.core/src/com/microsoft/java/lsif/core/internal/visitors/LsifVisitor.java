/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;
import com.microsoft.java.lsif.core.internal.protocol.SymbolData;

public class LsifVisitor extends ProtocolVisitor {

	public LsifVisitor(LsifService lsif, IndexerContext context) {
		super(lsif, context);
	}

	@Override
	public boolean visit(SimpleName node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node));
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node));
		return false;
	}

	private void resolve(int startPosition, int length, boolean needResolveImpl) {
		try {
			org.eclipse.lsp4j.Range sourceLspRange = JDTUtils.toRange(this.getContext().getCompilationUnit().getTypeRoot(),
					startPosition, length);

			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					sourceLspRange.getStart().getLine(), sourceLspRange.getStart().getCharacter(), new PreferenceManager(),
					new NullProgressMonitor());
			if (element == null) {
				return;
			}

			LsifService lsif = this.getLsif();
			Document docVertex = this.getContext().getDocVertex();
			Range sourceRange = Repository.getInstance().enlistRange(lsif, docVertex, sourceLspRange);

			Location definitionLocation = JdtlsUtils.getElementLocation(element);
			if (definitionLocation == null) {
				// not target location, only resolve hover.
				Hover hover = VisitorUtils.resolveHoverInformation(docVertex, sourceRange.getStart().getLine(),
						sourceRange.getStart().getCharacter());
				if (VisitorUtils.isEmptyHover(hover)) {
					return;
				}

				ResultSet resultSet = VisitorUtils.ensureResultSet(lsif, sourceRange);
				VisitorUtils.emitHoverResult(hover, lsif, resultSet);
				return;
			}

			String id = createSymbolKey(definitionLocation);
			SymbolData symbolData = Repository.getInstance().enlistSymbolData(id);

			/* Ensure resultSet */
			symbolData.ensureResultSet(lsif, sourceRange);

			/* Resolve definition */
			symbolData.resolveDefinition(lsif, definitionLocation);

			/* Resolve typeDefinition */
			symbolData.resolveTypeDefinition(lsif, docVertex, sourceLspRange);

			/* Resolve implementation */
			if (needResolveImpl) {
				symbolData.resolveImplementation(lsif, docVertex, sourceLspRange);
			}

			/* Resolve reference */
			symbolData.resolveReference(lsif, docVertex, definitionLocation, sourceRange);

			/* Resolve hover */
			symbolData.resolveHover(lsif, docVertex, sourceLspRange);
		} catch (Throwable ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping definition information ", ex);
		}
	}

	private String createSymbolKey(Location definitionLocation) {
		String rawKeyString = definitionLocation.toString();
		return DigestUtils.md5Hex(rawKeyString);
	}

	private boolean isTypeOrMethodDeclaration(ASTNode node) {
		return node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration;
	}
}
