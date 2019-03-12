/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.visitors;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ls.core.internal.handlers.DiagnosticsHandler;
import org.eclipse.lsp4j.Diagnostic;

import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;
import com.microsoft.java.lsif.core.internal.protocol.DiagnosticResult;
import com.microsoft.java.lsif.core.internal.protocol.Document;

public class DiagnosticVisitor extends ProtocolVisitor {

	private CompilationUnit cu;

	public DiagnosticVisitor(IndexerContext context, CompilationUnit cu) {
		this.setContext(context);
		this.cu = cu;
	}

	public DiagnosticResult enlist() {
		Emitter emitter = this.getContext().getEmitter();
		LsifService lsif = this.getContext().getLsif();
		Document docVertex = this.getContext().getDocVertex();
		IProblem[] problems = this.cu.getProblems();
		List<Diagnostic> diagnostics = DiagnosticsHandler.toDiagnosticsArray(this.cu.getJavaElement().getOpenable(),
				Arrays.asList(problems));

		DiagnosticResult diagnosticResult = lsif.getVertexBuilder().diagnosticResult(diagnostics);
		emitter.emit(diagnosticResult);
		emitter.emit(lsif.getEdgeBuilder().diagnostic(docVertex, diagnosticResult));
		return diagnosticResult;
	}
}
