/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */


package com.microsoft.java.lsif.core.internal.protocol;

public class DefinitionResult extends Vertex {

	public DefinitionResult(String id) {
		super(id, Vertex.DEFINITIONRESULT);
	}
}
