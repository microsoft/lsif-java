/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.indexer;

import com.microsoft.java.lsif.core.internal.protocol.Document;
import com.microsoft.java.lsif.core.internal.protocol.Edge;
import com.microsoft.java.lsif.core.internal.protocol.Project;
import com.microsoft.java.lsif.core.internal.protocol.Range;
import com.microsoft.java.lsif.core.internal.protocol.ReferenceItem;
import com.microsoft.java.lsif.core.internal.protocol.Vertex;

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

	public Edge hover(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_HOVER, from.getId(), to.getId());
	}

	public Edge referenceItem(Vertex from, Vertex to, String property) {
		return new ReferenceItem(generator.next(), Edge.ITEM, from.getId(), to.getId(), property);
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

	public Edge documentSymbols(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DOCUMENTSYMBOL, from.getId(), to.getId());
	}

	public Edge refersTo(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.REFERSTO, from.getId(), to.getId());
	}
}
