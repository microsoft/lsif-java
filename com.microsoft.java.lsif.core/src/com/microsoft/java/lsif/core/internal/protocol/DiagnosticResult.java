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
