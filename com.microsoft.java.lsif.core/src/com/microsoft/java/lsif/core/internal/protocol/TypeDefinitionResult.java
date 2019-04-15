/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.Arrays;
import java.util.List;

public class TypeDefinitionResult extends Vertex {

	private List<String> result;

	public TypeDefinitionResult(String id, String result) {
		super(id, Vertex.TYPEDEFINITIONRESULT);
		this.result = Arrays.asList(result);
	}

	public List<String> getResult() {
		return this.result;
	}
}
