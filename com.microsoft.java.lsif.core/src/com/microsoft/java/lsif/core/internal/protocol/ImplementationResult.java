/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class ImplementationResult extends Vertex {

	private List<Either<String, Location>> result;

	public ImplementationResult(String id, List<Either<String, Location>> result) {
		super(id, Vertex.IMPLEMENTATIONRESULT);
		this.result = result;
	}

	public List<Either<String, Location>> getResult() {
		return this.result;
	}
}
