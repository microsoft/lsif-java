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
