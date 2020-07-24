/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class Moniker extends Vertex {

	private String scheme = "jdt";

	private String kind;

	private String identifier;

	public Moniker(String id, String kind, String identifier) {
		super(id, Vertex.MONIKER);
		this.kind = kind;
		this.identifier = identifier;
		this.scheme = "jdt";
	}
}
