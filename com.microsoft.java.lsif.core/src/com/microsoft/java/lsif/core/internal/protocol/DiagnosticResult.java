/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;

public class DiagnosticResult extends Vertex {

	private List<Diagnostic> result;

	public DiagnosticResult(String id, List<Diagnostic> result) {
		super(id, Vertex.DIAGNOSTICRESULT);
		this.result = result;
	}

	public List<Diagnostic> getResult() {
		return this.result;
	}
}
