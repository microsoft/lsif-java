/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class EdgeBuilder {

	private IdGenerator generator;

	public EdgeBuilder(IdGenerator idGenerator) {
		this.generator = idGenerator;
	}

	public Edge contains(Project from, Document to) {
		return new Edge(generator.next(), Edge.CONTAINS, from.getId(), to.getId());
	}

	public Edge contains(Document from, Range to) {
		return new Edge(generator.next(), Edge.CONTAINS, from.getId(), to.getId());
	}

	public Edge contains(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.CONTAINS, from.getId(), to.getId());
	}

	public Edge definition(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DEFINITION, from.getId(), to.getId());
	}

	public Edge refersTo(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.REFERSTO, from.getId(), to.getId());
	}
}
