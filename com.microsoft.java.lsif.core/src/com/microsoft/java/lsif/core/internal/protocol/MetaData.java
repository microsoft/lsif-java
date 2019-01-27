/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class MetaData extends Vertex {

	public String version;

	public MetaData(String id, String version) {
		super(id, Vertex.METADATA);
		this.version = version;
	}
}
