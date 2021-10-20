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

public class Edge extends Element {

	public final static String CONTAINS = "contains";

	public final static String ITEM = "item";

	public final static String NEXT = "next";

	public final static String EXPORTS = "exports";

	public final static String IMPORTS = "imports";

	public final static String T_DOCUMENTSYMBOL = "textDocument/documentSymbol";

	public final static String T_FOLDINGRANGE = "textDocument/foldingRange";

	public final static String T_DOCUMENTLINK = "textDocument/documentLink";

	public final static String T_DIAGNOSTIC = "textDocument/diagnostic";

	public final static String T_DEFINITION = "textDocument/definition";

	public final static String T_DECLARATION = "textDocument/declaration";

	public final static String T_TYPEDEFINITION = "textDocument/typeDefinition";

	public final static String T_HOVER = "textDocument/hover";

	public final static String T_REFERENCES = "textDocument/references";

	public final static String T_IMPLEMENTATION = "textDocument/implementation";

	public final static String MONIKER = "moniker";

	public final static String ATTACH = "attach";

	public final static String PACKAGEINFORMATION = "packageInformation";

	public final static String BELONGSTO = "belongsTo";

	private String outV;

	private String inV;

	private List<String> inVs;

	public Edge(String id, String label, String outV, String inV) {
		super(id, Element.EDGE, label);
		this.outV = outV;
		this.inV = inV;
	}

	public Edge(String id, String label, String outV, List<String> inVs) {
		super(id, Element.EDGE, label);
		this.outV = outV;
		this.inVs = inVs;
	}

	public String getOutV() {
		return this.outV;
	}

	public String getInV() {
		return this.inV;
	}

	public List<String> getInVs() {
		return inVs;
	}
}
