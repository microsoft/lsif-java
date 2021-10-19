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

import com.microsoft.java.lsif.core.internal.IConstant;

public class Project extends Vertex {

	private String kind;

	@SuppressWarnings("unused")
	private String name;

	public Project(String id, String name) {
		super(id, Vertex.PROJECT);
		this.kind = IConstant.JAVA_ID;
		this.name = name;
	}

	public String getKind() {
		return this.kind;
	}
}
