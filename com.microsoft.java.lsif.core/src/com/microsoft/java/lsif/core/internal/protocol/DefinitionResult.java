/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class DefinitionResult extends Vertex {

	// TODO: Support bag result.
	private String result;

	public DefinitionResult(String id, String result) {
		super(id, Vertex.DEFINITIONRESULT);
		this.result = result;
	}

	public String getResult() {
		return this.result;
	}
}
