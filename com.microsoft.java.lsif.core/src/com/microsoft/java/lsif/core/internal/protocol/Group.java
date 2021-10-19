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

public class Group extends Vertex {

	public enum ConflictResolution {
		TAKEDUMP("takeDump"), TAKEDB("takeDB");

		private final String resolution;

		private ConflictResolution(String resolution) {
			this.resolution = resolution;
		}

		@Override
		public String toString() {
			return this.resolution;
		}
	}

	@SuppressWarnings("unused")
	private String uri;

	@SuppressWarnings("unused")
	private String resolution;

	@SuppressWarnings("unused")
	private String name;

	@SuppressWarnings("unused")
	private String rootUri;

	public Group(String id, String uri, ConflictResolution resolution, String name, String rootUri) {
		super(id, Vertex.GROUP);
		this.uri = uri;
		this.resolution = resolution.toString();
		this.name = name;
		this.rootUri = rootUri;
	}

}
