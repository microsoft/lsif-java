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
