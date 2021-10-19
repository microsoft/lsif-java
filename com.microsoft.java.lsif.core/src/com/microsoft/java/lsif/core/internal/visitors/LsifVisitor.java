/*******************************************************************************
* Copyright (c) 2021 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.indexer.Repository;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ResultSet;

public class LsifVisitor extends ProtocolVisitor {

	private boolean hasPackageInformation;

	public LsifVisitor(LsifService lsif, IndexerContext context, boolean hasPackageInformation) {
		super(lsif, context);
		this.hasPackageInformation = hasPackageInformation;
	}

	@Override
	public boolean visit(SimpleName node) {
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		int modifier = binding.getModifiers();
		ASTNode parent = node.getParent();
		if (parent instanceof EnumConstantDeclaration) {
			ASTNode enumDeclaration = parent.getParent();
			if (enumDeclaration instanceof EnumDeclaration) {
				modifier = ((EnumDeclaration) enumDeclaration).getModifiers();
			}
		}
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), modifier);
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		int modifier = binding.getModifiers();
		resolve(node.getStartPosition(), node.getLength(), isTypeOrMethodDeclaration(node), modifier);
		return false;
	}

	private void resolve(int startPosition, int length, boolean needResolveImpl, int modifier) {
		try {
			org.eclipse.lsp4j.Range sourceLspRange = JDTUtils
					.toRange(this.getContext().getCompilationUnit().getTypeRoot(), startPosition, length);
			IJavaElement element = JDTUtils.findElementAtSelection(this.getContext().getCompilationUnit().getTypeRoot(),
					sourceLspRange.getStart().getLine(), sourceLspRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
			if (element == null) {
				return;
			}
			LsifService lsif = this.getLsif();
			Document docVertex = this.getContext().getDocVertex();
			Project projVertex = this.getContext().getProjVertex();
			Range sourceRange = Repository.getInstance().enlistRange(lsif, docVertex, sourceLspRange);

			Location definitionLocation = JdtlsUtils.getElementLocation(element);
			if (definitionLocation == null) {
				// no target location, only resolve hover.
				Hover hover = VisitorUtils.resolveHoverInformation(docVertex, sourceRange.getStart().getLine(),
						sourceRange.getStart().getCharacter());
				if (VisitorUtils.isEmptyHover(hover)) {
					return;
				}
				ResultSet resultSet = VisitorUtils.ensureResultSet(lsif, sourceRange);
				VisitorUtils.emitHoverResult(hover, lsif, resultSet);
				return;
			}

			String id = VisitorUtils.createSymbolKey(definitionLocation);
			Document definitionDocument = Repository.getInstance().enlistDocument(lsif, definitionLocation.getUri(),
					projVertex);
			SymbolData symbolData = Repository.getInstance().enlistSymbolData(id, definitionDocument, projVertex);
			/* Ensure resultSet */
			symbolData.ensureResultSet(lsif, sourceRange);

			/* Resolve moniker */
			symbolData.resolveMoniker(lsif, element, modifier, hasPackageInformation);

			/* Resolve definition */
			symbolData.resolveDefinition(lsif, element, definitionLocation);

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
		} catch (Exception e) {
			JavaLanguageServerPlugin.logException(e.getMessage(), e);
		}

	}

	private boolean isTypeOrMethodDeclaration(ASTNode node) {
		return node.getParent() instanceof TypeDeclaration || node.getParent() instanceof MethodDeclaration;
	}

}
