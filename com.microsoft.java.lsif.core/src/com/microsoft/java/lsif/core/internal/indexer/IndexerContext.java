/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Project;

public class IndexerContext {

	private Document docVertex;

	private CompilationUnit compilationUnit;

	private Project projVertex;

	public IndexerContext(Document docVertex, CompilationUnit compilationUnit, Project projVertex) {
		this.setDocVertex(docVertex);
		this.setCompilationUnit(compilationUnit);
		this.setProjVertex(projVertex);
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

	/**
	 * @return the projVertex
	 */
	public Project getProjVertex() {
		return projVertex;
	}

	/**
	 * @param projVertex the projVertex to set
	 */
	public void setProjVertex(Project projVertex) {
		this.projVertex = projVertex;
	}
}
