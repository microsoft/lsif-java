/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;

import com.microsoft.java.lsif.core.internal.indexer.IndexerContext;

public abstract class ProtocolVisitor extends ASTVisitor {

	private IndexerContext context;

	/**
	 * Constructor
	 */
	public ProtocolVisitor() {
	}

	public IndexerContext getContext() {
		return this.context;
	}

	public void setContext(IndexerContext context) {
		this.context = context;
	}
}
