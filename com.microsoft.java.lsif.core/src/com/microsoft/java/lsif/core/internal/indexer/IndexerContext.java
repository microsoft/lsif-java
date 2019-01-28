/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.jdt.core.ITypeRoot;

import com.microsoft.java.lsif.core.internal.emitter.Emitter;
import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.JavaLsif;

public class IndexerContext {

	private Emitter emitter;

	private JavaLsif lsif;

	private Document docVertex;

	private ITypeRoot typeRoot;

	public IndexerContext(Emitter emitter, JavaLsif lsif, Document docVertex, ITypeRoot typeRoot) {
		this.setEmitter(emitter);
		this.setLsif(lsif);
		this.setDocVertex(docVertex);
		this.setTypeRoot(typeRoot);

	}

	public Emitter getEmitter() {
		return this.emitter;
	}

	/**
	 * @return the lsif
	 */
	public JavaLsif getLsif() {
		return lsif;
	}


	public Document getDocVertex() {
		return this.docVertex;
	}

	/**
	 * @return the typeRoot
	 */
	public ITypeRoot getTypeRoot() {
		return typeRoot;
	}

	/**
	 * @param typeRoot the typeRoot to set
	 */
	public void setTypeRoot(ITypeRoot typeRoot) {
		this.typeRoot = typeRoot;
	}

	/**
	 * @param emitter the emitter to set
	 */
	public void setEmitter(Emitter emitter) {
		this.emitter = emitter;
	}


	/**
	 * @param lsif the lsif to set
	 */
	public void setLsif(JavaLsif lsif) {
		this.lsif = lsif;
	}

	/**
	 * @param docVertex the docVertex to set
	 */
	public void setDocVertex(Document docVertex) {
		this.docVertex = docVertex;
	}
}
