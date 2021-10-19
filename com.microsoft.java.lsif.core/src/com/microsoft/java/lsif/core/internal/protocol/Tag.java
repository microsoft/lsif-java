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

public abstract class Tag {

	public static final String DECLARATION = "declaration";

	public static final String DEFINITION = "definition";

	public static final String REFERENCE = "reference";

	public static final String UNKNOWN = "unknown";

	private String type;

	private String text;

	public Tag(String type, String text) {
		this.type = type;
		this.text = text;
	}

	public String getType() {
		return this.type;
	}

	public String getText() {
		return this.text;
	}
}
