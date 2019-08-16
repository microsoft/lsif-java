/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

import java.util.List;

public class ItemEdge extends Edge {

	private String document;

	public ItemEdge(String id, String label, String outV, String inV, String documentId) {
		super(id, label, outV, inV);
		this.document = documentId;
	}

	public ItemEdge(String id, String label, String outV, List<String> inVs, String documentId) {
		super(id, label, outV, inVs);
		this.document = documentId;
	}

	public String getDocument() {
		return document;
	}
}
