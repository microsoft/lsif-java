/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ls.core.internal.preferences.PreferenceManager;

import com.microsoft.java.lsif.core.internal.protocol.Document;

public class IndexerContext {

	private LsifService lsif;

	private Document docVertex;

	private ITypeRoot typeRoot;

	private PreferenceManager preferenceManger;

	public IndexerContext(LsifService lsif, Document docVertex, ITypeRoot typeRoot,
			PreferenceManager preferenceManager) {
		this.setLsif(lsif);
		this.setDocVertex(docVertex);
		this.setTypeRoot(typeRoot);
		this.setPreferenceManger(preferenceManager);
	}

	/**
	 * @return the lsif
	 */
	public LsifService getLsif() {
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
	 * @param lsif the lsif to set
	 */
	public void setLsif(LsifService lsif) {
		this.lsif = lsif;
	}

	/**
	 * @param docVertex the docVertex to set
	 */
	public void setDocVertex(Document docVertex) {
		this.docVertex = docVertex;
	}

	/**
	 * @return the preferenceManger
	 */
	public PreferenceManager getPreferenceManger() {
		return preferenceManger;
	}

	/**
	 * @param preferenceManger the preferenceManger to set
	 */
	public void setPreferenceManger(PreferenceManager preferenceManger) {
		this.preferenceManger = preferenceManger;
	}
}
