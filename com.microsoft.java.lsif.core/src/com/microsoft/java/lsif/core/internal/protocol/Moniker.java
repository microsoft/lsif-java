/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class Moniker extends Vertex {

	private String kind;

	private String scheme;

	private String identifier;

	private String unique;

	public Moniker(String id, String kind, String scheme, String identifier, String unique) {
		super(id, Vertex.MONIKER);
		this.kind = kind;
		this.identifier = identifier;
		this.scheme = scheme;
		this.unique = unique;
	}
}
