/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.visitors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.handlers.JsonRpcHelpers;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.m2e.core.internal.IMavenConstants;

import com.microsoft.java.lsif.core.internal.LanguageServerIndexerPlugin;
import com.microsoft.java.lsif.core.internal.emitter.LsifEmitter;
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

	public void enlist() {
		LsifService lsif = this.getContext().getLsif();
		Document docVertex = this.getContext().getDocVertex();
		IResource resource = cu.getJavaElement().getResource();
		if (resource == null || !resource.exists()) {
			LanguageServerIndexerPlugin.logError("Cannot find resource for: " + cu.getJavaElement().getElementName());
			return;
		}
		IMarker[] markers = null;
		IDocument document = null;
		try {
			IMarker[] javaMarkers = resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
					IResource.DEPTH_ONE);
			IMarker[] taskMarkers = resource.findMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_ONE);
			int totalLength = javaMarkers.length + taskMarkers.length;
			if (totalLength == 0) {
				return;
			}
			markers = Arrays.copyOf(javaMarkers, javaMarkers.length + taskMarkers.length);
			System.arraycopy(taskMarkers, 0, markers, javaMarkers.length, taskMarkers.length);
			document = JsonRpcHelpers.toDocument(cu.getJavaElement().getOpenable().getBuffer());
		} catch (CoreException ex) {
			LanguageServerIndexerPlugin.logException("Exception when dumping diagnostics ", ex);
			return;
		}

		if (document == null) {
			LanguageServerIndexerPlugin
					.logError("Cannot parse the document for: " + cu.getJavaElement().getElementName());
			return;
		}

		List<Diagnostic> diagnostics = toDiagnosticsArray(document, markers);
		DiagnosticResult diagnosticResult = lsif.getVertexBuilder().diagnosticResult(diagnostics);
		LsifEmitter.getInstance().emit(diagnosticResult);
		LsifEmitter.getInstance().emit(lsif.getEdgeBuilder().diagnostic(docVertex, diagnosticResult));
	}

	private List<Diagnostic> toDiagnosticsArray(IDocument document, IMarker[] markers) {
		List<Diagnostic> diagnostics = Stream.of(markers).map(m -> toDiagnostic(document, m)).filter(d -> d != null)
				.collect(Collectors.toList());
		return diagnostics;
	}

	private static Diagnostic toDiagnostic(IDocument document, IMarker marker) {
		if (marker == null || !marker.exists()) {
			return null;
		}
		Diagnostic d = new Diagnostic();
		d.setSource(JavaLanguageServerPlugin.SERVER_SOURCE_ID);
		d.setMessage(marker.getAttribute(IMarker.MESSAGE, ""));
		d.setCode(String.valueOf(marker.getAttribute(IJavaModelMarker.ID, 0)));
		d.setSeverity(convertSeverity(marker.getAttribute(IMarker.SEVERITY, -1)));
		d.setRange(convertRange(document, marker));
		return d;
	}

	private static Range convertRange(IDocument document, IMarker marker) {
		int line = marker.getAttribute(IMarker.LINE_NUMBER, -1) - 1;
		int cStart = 0;
		int cEnd = 0;
		try {
			// Buildship doesn't provide markers for gradle files, Maven does
			if (marker.isSubtypeOf(IMavenConstants.MARKER_ID)) {
				cStart = marker.getAttribute(IMavenConstants.MARKER_COLUMN_START, -1);
				cEnd = marker.getAttribute(IMavenConstants.MARKER_COLUMN_END, -1);
			} else {
				int lineOffset = 0;
				try {
					lineOffset = document.getLineOffset(line);
				} catch (BadLocationException unlikelyException) {
					JavaLanguageServerPlugin.logException(unlikelyException.getMessage(), unlikelyException);
					return new Range(new Position(line, 0), new Position(line, 0));
				}
				cEnd = marker.getAttribute(IMarker.CHAR_END, -1) - lineOffset;
				cStart = marker.getAttribute(IMarker.CHAR_START, -1) - lineOffset;
			}
		} catch (CoreException e) {
			LanguageServerIndexerPlugin.logException(e.getMessage(), e);
		}
		cStart = Math.max(0, cStart);
		cEnd = Math.max(0, cEnd);

		return new Range(new Position(line, cStart), new Position(line, cEnd));
	}

	private static DiagnosticSeverity convertSeverity(int severity) {
		if (severity == IMarker.SEVERITY_ERROR) {
			return DiagnosticSeverity.Error;
		}
		if (severity == IMarker.SEVERITY_WARNING) {
			return DiagnosticSeverity.Warning;
		}
		return DiagnosticSeverity.Information;
	}
}
