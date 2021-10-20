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

public abstract class Element {

	public final static String VERTEX = "vertex";

	public final static String EDGE = "edge";

	private String id;

	private String type;

	private String label;

	public Element(String id, String type, String label) {
		this.id = id;
		this.type = type;
		this.label = label;
	}

	public String getId() {
		return this.id;
	}

	public String getType() {
		return this.type;
	}

	public String getLabel() {
		return this.label;
	}
}
