/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;

import com.microsoft.java.lsif.core.internal.JdtlsUtils;
import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.JavaLsif;
import com.microsoft.java.lsif.core.internal.protocol.Vertex;

public class LsifVisitor extends ASTVisitor {

	private Emitter emitter;

	private Document docVertext;

	private ITypeRoot typeRoot;

	private JavaLsif lsif;

	public LsifVisitor(ITypeRoot typeRoot, Emitter emitter, Document docVertex, JavaLsif lsif) {
		this.typeRoot = typeRoot;
		this.emitter = emitter;
		this.docVertext = docVertex;
		this.lsif = lsif;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.
	 * SingleVariableDeclaration)
	 */
	@Override
	public boolean visit(SingleVariableDeclaration node) {
		Type declarationType = node.getType();
		try {
			Range fromRange = JDTUtils.toRange(this.typeRoot, declarationType.getStartPosition(), declarationType.getLength());

			IJavaElement element = JDTUtils.findElementAtSelection(this.typeRoot, fromRange.getStart().getLine(), fromRange.getStart().getCharacter(),
					new PreferenceManager(), new NullProgressMonitor());
			if (element == null) {
				return false;
			}

			Location targetLocation = null;
			ICompilationUnit compilationUnit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
			IClassFile cf = (IClassFile) element.getAncestor(IJavaElement.CLASS_FILE);
			if (compilationUnit != null || (cf != null && cf.getSourceRange() != null)) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(element), this.typeRoot.getJavaProject());
			}
			if (element instanceof IMember && ((IMember) element).getClassFile() != null) {
				targetLocation = JdtlsUtils.fixLocation(element, JDTUtils.toLocation(((IMember) element).getClassFile()), this.typeRoot.getJavaProject());
			}

			if (targetLocation == null) {
				return false;
			}

			Vertex from = lsif.getVertexBuilder().range(docVertext.getId(), fromRange);
			emitter.emit(from);
			emitter.emit(lsif.getEdgeBuilder().contains(docVertext, from));

			Range targetRange = targetLocation.getRange();

			Document targetDocument = lsif.getVertexBuilder().getDocument(targetLocation.getUri());
			if (targetDocument == null) {
				targetDocument = lsif.getVertexBuilder().document(targetLocation.getUri());
				emitter.emit(targetDocument);
			}

			Vertex to = lsif.getVertexBuilder().getRange(targetDocument.getId(), targetRange);
			if (to == null) {
				to = lsif.getVertexBuilder().range(targetDocument.getId(), targetRange);
				emitter.emit(to);
				emitter.emit(lsif.getEdgeBuilder().contains(targetDocument, to));
			}

			emitter.emit(lsif.getEdgeBuilder().definition(from, to));

		} catch (CoreException e) {
			LanguageServerIndexerPlugin.log(e);
		}

		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		ITypeBinding declClazz = methodBinding.getDeclaringClass();
		if (declClazz == null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		IBinding binding = node.resolveBinding();
		if (binding == null || !(binding instanceof IVariableBinding)) {
			return false;
		}

		ITypeBinding declClazz = ((IVariableBinding) binding).getDeclaringClass();
		if (declClazz == null) {
			return false;
		}

		return true;
	}
}
