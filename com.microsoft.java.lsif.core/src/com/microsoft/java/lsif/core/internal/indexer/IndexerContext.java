/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.microsoft.java.lsif.core.internal.protocol.Document;

public class IndexerContext {

	private Document docVertex;

	private CompilationUnit compilationUnit;

	public IndexerContext(Document docVertex, CompilationUnit compilationUnit) {
		this.setDocVertex(docVertex);
		this.setCompilationUnit(compilationUnit);
	}

	public Document getDocVertex() {
		return this.docVertex;
	}

	/**
	 * @param docVertex the docVertex to set
	 */
	public void setDocVertex(Document docVertex) {
		this.docVertex = docVertex;
	}

	/**
	 * @return the compilationUnit
	 */
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * @param compilationUnit the compilationUnit to set
	 */
	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}
}
