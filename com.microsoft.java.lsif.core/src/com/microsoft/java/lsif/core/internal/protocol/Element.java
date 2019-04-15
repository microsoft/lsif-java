/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public abstract class Element {

	public final static String VERTEX = "vertex";

	public final static String EDGE = "edge";

	private String id;

	private String type;

	private String label;

	public Element(String id, String type, String label) {
		this.id = id;
		this.type = type;
		this.label = label;
	}

	public String getId() {
		return this.id;
	}

	public String getType() {
		return this.type;
	}

	public String getLabel() {
		return this.label;
	}
}
