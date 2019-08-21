/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import com.microsoft.java.lsif.core.internal.IConstant;

public class MetaData extends Vertex {

	private static final String DEFAULT_ENCODING = "utf-16";

	public String version;

	// URI String
	public String projectRoot;

	public String positionEncoding = DEFAULT_ENCODING;

	public MetaData(String id, String projectRoot) {
		super(id, Vertex.METADATA);
		this.version = IConstant.LSIF_FORMAT_VERSION;
		this.projectRoot = projectRoot;
	}
}
