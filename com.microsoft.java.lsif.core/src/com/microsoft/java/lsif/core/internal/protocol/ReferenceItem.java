/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class ReferenceItem extends Edge {

	public final static String DECLARATION = "declaration";

	public final static String DEFINITION = "definition";

	public final static String REFERENCE = "reference";

	private String property;

	public ReferenceItem(String id, String label, String outV, String inV, String property) {
		super(id, label, outV, inV);
		this.property = property;
	}

	public String getProperty() {
		return this.property;
	}
}
