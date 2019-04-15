/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

public class ReferenceResult extends Vertex {

	private List<String> declarations;

	private List<String> definitions;

	private List<String> references;

	private List<String> referenceResults;

	public ReferenceResult(String id) {
		super(id, Vertex.REFERENCERESULT);
	}

	public ReferenceResult(String id, List<String> declarations, List<String> definitions, List<String> references, List<String> referenceResults) {
		super(id, Vertex.REFERENCERESULT);
		this.declarations = declarations;
		this.definitions = definitions;
		this.references = references;
		this.referenceResults = referenceResults;
	}

	public List<String> getDeclarations() {
		return this.declarations;
	}

	public List<String> getDefinitions() {
		return this.definitions;
	}

	public List<String> getReferences() {
		return this.references;
	}

	public List<String> getReferenceResults() {
		return this.referenceResults;
	}
}
