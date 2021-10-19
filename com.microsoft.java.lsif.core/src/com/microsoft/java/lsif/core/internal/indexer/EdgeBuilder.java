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

	public Edge item(Vertex from, Vertex to, Document doc, String property) {
		return new ItemEdge(generator.next(), Edge.ITEM, from.getId(), Collections.singletonList(to.getId()),
				doc.getId(), property);
	}

	public Edge item(Vertex from, List<String> inVs, Document doc, String property) {
		return new ItemEdge(generator.next(), Edge.ITEM, from.getId(), inVs, doc.getId(), property);
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

	public Edge moniker(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.MONIKER, from.getId(), to.getId());
	}

	public Edge attach(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.ATTACH, from.getId(), to.getId());
	}

	public Edge packageInformation(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.PACKAGEINFORMATION, from.getId(), to.getId());
	}

	public Edge diagnostic(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.T_DIAGNOSTIC, from.getId(), to.getId());
	}

	public Edge belongsTo(Vertex from, Vertex to) {
		return new Edge(generator.next(), Edge.BELONGSTO, from.getId(), to.getId());
	}
}
