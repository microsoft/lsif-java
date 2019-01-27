/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

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
