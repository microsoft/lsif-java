/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import com.microsoft.java.lsif.core.internal.IConstant;

public class Project extends Vertex {

	private String kind;

	@SuppressWarnings("unused")
	private String name;

	public Project(String id, String name) {
		super(id, Vertex.PROJECT);
		this.kind = IConstant.JAVA_ID;
		this.name = name;
	}

	public String getKind() {
		return this.kind;
	}
}
