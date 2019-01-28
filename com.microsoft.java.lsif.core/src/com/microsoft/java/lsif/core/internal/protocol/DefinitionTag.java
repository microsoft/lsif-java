/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import org.eclipse.lsp4j.SymbolKind;

public class DefinitionTag extends Tag {

	/**
	 * The symbol kind.
	 */
	private SymbolKind kind;

	/**
	 * Indicates if this symbol is deprecated.
	 */
	private Boolean deprecated;

	/**
	 * The full range of the definition not including leading/trailing whitespace
	 * but everything else, e.g comments and code. The range must be included in
	 * fullRange.
	 */
	private org.eclipse.lsp4j.Range fullRange;

	/**
	 * Optional detail information for the definition.
	 */
	private String detail;

	public DefinitionTag(String text, SymbolKind kind, Boolean deprecated, org.eclipse.lsp4j.Range fullRange, String detail) {
		super(Tag.DEFINITION, text);
		this.kind = kind;
		this.deprecated = deprecated;
		this.fullRange = fullRange;
		this.detail = detail;
	}

	public SymbolKind getKind() {
		return this.kind;
	}

	public Boolean getDeprecated() {
		return this.deprecated;
	}

	public org.eclipse.lsp4j.Range getFullRange() {
		return this.fullRange;
	}

	public String getDetail() {
		return this.detail;
	}
}
