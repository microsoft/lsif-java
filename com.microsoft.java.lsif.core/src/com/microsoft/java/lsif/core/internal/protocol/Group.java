/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

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
