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
