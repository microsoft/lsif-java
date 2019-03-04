/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class Edge extends Element {

	public final static String CONTAINS = "contains";

	public final static String ITEM = "item";

	public final static String REFERSTO = "refersTo";

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

	private String outV;

	private String inV;

	public Edge(String id, String label, String outV, String inV) {
		super(id, Element.EDGE, label);
		this.outV = outV;
		this.inV = inV;
	}

	public String getOutV() {
		return this.outV;
	}

	public String getInV() {
		return this.inV;
	}
}
