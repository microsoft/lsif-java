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

public class Event extends Vertex {

	private String kind;

	private String scope;

	private String data;

	public Event(String id, String scope, String kind, String data) {
		super(id, Vertex.EVENT);
		this.scope = scope;
		this.kind = kind;
		this.data = data;
	}

	public String getScope() {
		return scope;
	}

	public String getKind() {
		return kind;
	}

	public String getData() {
		return data;
	}

	public static class EventScope {
		public static final String Group = "group";
		public static final String Project = "project";
		public static final String DOCUMENT = "document";
	}

	public static class EventKind {
		public static final String BEGIN = "begin";
		public static final String END = "end";
	}
}
