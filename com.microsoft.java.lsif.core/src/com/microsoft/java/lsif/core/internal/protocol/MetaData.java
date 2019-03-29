/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import com.microsoft.java.lsif.core.internal.IConstant;

public class MetaData extends Vertex {

	public String version;

	public MetaData(String id) {
		super(id, Vertex.METADATA);
		this.version = IConstant.LSIF_FORMAT_VERSION;
	}
}
