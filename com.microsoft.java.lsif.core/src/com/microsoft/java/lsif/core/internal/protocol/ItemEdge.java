/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

public class ItemEdge extends Edge {

	private String document;

	private String property;

	public ItemEdge(String id, String label, String outV, String inV, String documentId, String property) {
		super(id, label, outV, inV);
		this.document = documentId;
		this.property = property;
	}

	public ItemEdge(String id, String label, String outV, List<String> inVs, String documentId, String property) {
		super(id, label, outV, inVs);
		this.document = documentId;
		this.property = property;
	}

	public String getDocument() {
		return document;
	}

	public String getProperty() {
		return property;
	}

	public static class ItemEdgeProperties {
		public static final String DECLARATIONS = "declarations";
		public static final String DEFINITIONS = "definitions";
		public static final String REFERENCES = "references";
		public static final String REFERENCE_RESULTS = "referenceResults";
		public static final String IMPLEMENTATION_RESULTS = "implementationResults";
	}
}
