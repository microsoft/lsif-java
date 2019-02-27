/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.microsoft.java.lsif.core.internal.visitors.DefinitionVisitor;
import com.microsoft.java.lsif.core.internal.visitors.HoverVisitor;
import com.microsoft.java.lsif.core.internal.visitors.ReferencesVisitor;
import com.microsoft.java.lsif.core.internal.visitors.TypeDefinitionVisitor;

public class LsifVisitor extends ASTVisitor {

	private DefinitionVisitor defVisitor;
	private ReferencesVisitor refVisitor;
	private TypeDefinitionVisitor typeDefVisitor;
	private HoverVisitor hoverVisitor;

	public LsifVisitor(IndexerContext context) {
		this.defVisitor = new DefinitionVisitor(context);
		this.refVisitor = new ReferencesVisitor(context);
		this.typeDefVisitor = new TypeDefinitionVisitor(context);
		this.hoverVisitor = new HoverVisitor(context);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		defVisitor.handle(node);
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		if (node.getParent() instanceof TypeDeclaration) {
			defVisitor.handle(node);
			hoverVisitor.handle(node);
			return false;
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		refVisitor.handle(node);
		return true;
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
