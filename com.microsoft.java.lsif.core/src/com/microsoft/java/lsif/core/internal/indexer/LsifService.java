/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import com.microsoft.java.lsif.core.internal.indexer.IdGenerator.IdType;

public class LsifService {

	private IdGenerator generator;

	private VertexBuilder vBuilder;

	private EdgeBuilder eBuilder;

	public LsifService() {
		this.generator = new IdGenerator(IdType.COUNTER);
		this.vBuilder = new VertexBuilder(generator);
		this.eBuilder = new EdgeBuilder(generator);
	}

	public VertexBuilder getVertexBuilder() {
		return this.vBuilder;
	}

	public EdgeBuilder getEdgeBuilder() {
		return this.eBuilder;
	}
}
