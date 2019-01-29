/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;

import com.microsoft.java.lsif.core.internal.IConstant;

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

		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() {
				try {
					if (progressMonitor != null && progressMonitor.isCanceled()) {
						return;
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
						root[0] = (CompilationUnit) parser.createAST(progressMonitor);
					}
					// mark as unmodifiable
					ASTNodes.setFlagsToAST(root[0], ASTNode.PROTECT);
				} catch (OperationCanceledException ex) {
					return;
				} catch (JavaModelException e) {
					JavaLanguageServerPlugin.logException(e.getMessage(), e);
					return;
				}
			}

			@Override
			public void handleException(Throwable ex) {
				IStatus status = new Status(IStatus.ERROR, IConstant.PLUGIN_ID, IStatus.OK, "Error in JDT Core during AST creation", ex); //$NON-NLS-1$
				JavaLanguageServerPlugin.log(status);
			}
		});
		return root[0];
	}

	public static ASTParser newASTParser() {
		final ASTParser parser = ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(IASTSharedValues.SHARED_AST_STATEMENT_RECOVERY);
		parser.setBindingsRecovery(IASTSharedValues.SHARED_BINDING_RECOVERY);
		return parser;
	}

}
