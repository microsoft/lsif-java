/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import java.util.Collections;
import java.util.List;

import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Edge;
import com.microsoft.java.lsif.core.internal.protocol.ItemEdge;
import com.microsoft.java.lsif.core.internal.protocol.Vertex;

public class EdgeBuilder {

	private IdGenerator generator;

	public EdgeBuilder(IdGenerator idGenerator) {
		this.generator = idGenerator;
	}

	public Edge contains(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.CONTAINS, from.getId(), Collections.singletonList(to.getId()));
	}

	public Edge item(Vertex from, Vertex to, Document doc) {
		return new ItemEdge(generator.next(), Edge.ITEM, from.getId(), Collections.singletonList(to.getId()),
				doc.getId());
	}

	public Edge item(Vertex from, List<String> inVs, Document doc) {
		return new ItemEdge(generator.next(), Edge.ITEM, from.getId(), inVs, doc.getId());
	}

	public Edge hover(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_HOVER, from.getId(), to.getId());
	}

	public Edge definition(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DEFINITION, from.getId(), to.getId());
	}

	public Edge typeDefinition(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_TYPEDEFINITION, from.getId(), to.getId());
	}

	public Edge references(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_REFERENCES, from.getId(), to.getId());
	}

	public Edge implementation(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_IMPLEMENTATION, from.getId(), to.getId());
	}

	public Edge documentSymbols(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DOCUMENTSYMBOL, from.getId(), to.getId());
	}

	public Edge next(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.NEXT, from.getId(), to.getId());
	}

	public Edge diagnostic(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DIAGNOSTIC, from.getId(), to.getId());
	}
}
