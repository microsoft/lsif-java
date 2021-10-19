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
