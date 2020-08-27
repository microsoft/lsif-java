/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

public final class ASTUtil {

	/**
	 * Creates a new compilation unit AST.
	 *
	 * @param input           the Java element for which to create the AST
	 * @param progressMonitor the progress monitor
	 * @return AST
	 */
	public static CompilationUnit createAST(final ITypeRoot input, final IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) {
			return null;
		}

		final CompilationUnit root[] = new CompilationUnit[1];

		try {
			if (progressMonitor != null && progressMonitor.isCanceled()) {
				return null;
			}
			if (input instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) input;
				if (cu.isWorkingCopy()) {
					root[0] = cu.reconcile(IASTSharedValues.SHARED_AST_LEVEL, true, null, progressMonitor);
				}
			}
			if (root[0] == null) {
				final ASTParser parser = newASTParser();
				parser.setSource(input);
				parser.setResolveBindings(true);
				parser.setBindingsRecovery(true);
				root[0] = (CompilationUnit) parser.createAST(progressMonitor);
			}
			// mark as unmodifiable
			ASTNodes.setFlagsToAST(root[0], ASTNode.PROTECT);
		} catch (OperationCanceledException ex) {
			return null;
		} catch (Throwable e) {
			JavaLanguageServerPlugin.logException("Error in JDT Core during AST creation", e);
			return null;
		}
		return root[0];
	}

	public static ASTParser newASTParser() {
		final ASTParser parser = ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(IASTSharedValues.SHARED_AST_STATEMENT_RECOVERY);
		return parser;
	}

}
