/*******************************************************************************
* Copyright (c) 2021 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

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
