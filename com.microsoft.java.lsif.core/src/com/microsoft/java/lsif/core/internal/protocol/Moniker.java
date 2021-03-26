/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class Moniker extends Vertex {

	public enum MonikerKind {
		EXPORT("export"), IMPORT("import"), LOCAL("local");

		private final String kind;

		private MonikerKind(String kind) {
			this.kind = kind;
		}

		@Override
		public String toString() {
			return this.kind;
		}
	}

	public enum MonikerUnique {
		GROUP("group"), SCHEME("scheme");

		private final String unique;

		private MonikerUnique(String unique) {
			this.unique = unique;
		}

		@Override
		public String toString() {
			return this.unique;
		}
	}

	@SuppressWarnings("unused")
	private String kind;

	@SuppressWarnings("unused")
	private String scheme;

	@SuppressWarnings("unused")
	private String identifier;

	@SuppressWarnings("unused")
	private String unique;

	public Moniker(String id, MonikerKind kind, String scheme, String identifier, MonikerUnique unique) {
		super(id, Vertex.MONIKER);
		this.kind = kind.toString();
		this.identifier = identifier;
		this.scheme = scheme;
		this.unique = unique.toString();
	}
}
