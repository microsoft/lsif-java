/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;

import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;
import com.microsoft.java.lsif.core.internal.indexer.LsifService;

public abstract class ProtocolVisitor extends ASTVisitor {

	private LsifService lsif;

	private IndexerContext context;

	/**
	 * Constructor
	 */
	public ProtocolVisitor(LsifService lsif, IndexerContext context) {
		this.setLsif(lsif);
		this.setContext(context);
	}

	/**
	 * @return the service
	 */
	public LsifService getLsif() {
		return lsif;
	}

	/**
	 * @param service the service to set
	 */
	public void setLsif(LsifService lsif) {
		this.lsif = lsif;
	}

	/**
	 * @return the context
	 */
	public IndexerContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(IndexerContext context) {
		this.context = context;
	}
}
