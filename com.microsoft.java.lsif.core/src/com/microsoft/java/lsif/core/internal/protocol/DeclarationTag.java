/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import org.eclipse.lsp4j.SymbolKind;

public class DeclarationTag extends Tag {

	@SuppressWarnings("unused")
	private SymbolKind kind;

	@SuppressWarnings("unused")
	private org.eclipse.lsp4j.Range fullRange;

	public DeclarationTag(String text) {
		super(Tag.DECLARATION, text);
	}
}
