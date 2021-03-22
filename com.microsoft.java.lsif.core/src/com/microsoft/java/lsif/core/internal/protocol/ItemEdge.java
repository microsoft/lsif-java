/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

public class ItemEdge extends Edge {

	private String shard;

	private String property;

	public ItemEdge(String id, String label, String outV, String inV, String shardId, String property) {
		super(id, label, outV, inV);
		this.shard = shardId;
		this.property = property;
	}

	public ItemEdge(String id, String label, String outV, List<String> inVs, String shardId, String property) {
		super(id, label, outV, inVs);
		this.shard = shardId;
		this.property = property;
	}

	public String getShard() {
		return shard;
	}

	public String getProperty() {
		return property;
	}

	public static class ItemEdgeProperties {
		public static final String DECLARATIONS = "declarations";
		public static final String DEFINITIONS = "definitions";
		public static final String REFERENCES = "references";
		public static final String REFERENCE_RESULTS = "referenceResults";
		public static final String REFERENCE_LINKS = "referenceLinks";
		public static final String IMPLEMENTATION_RESULTS = "implementationResults";
	}
}
