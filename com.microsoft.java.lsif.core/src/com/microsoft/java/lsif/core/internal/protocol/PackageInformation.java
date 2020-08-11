/* --------------------------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 * ------------------------------------------------------------------------------------------ */

package com.microsoft.java.lsif.core.internal.protocol;

public class PackageInformation extends Vertex {

	public enum PackageManager {
		MAVEN("maven"), GRADLE("gradle"), JDK("jdk");

		private final String manager;

		private PackageManager(String manager) {
			this.manager = manager;
		}

		@Override
		public String toString() {
			return this.manager;
		}
	}

	private String name;

	private String manager;

	private String version;

	private Repo repository;

	public PackageInformation(String id, String name, PackageManager manager, String version, String type, String url) {
		super(id, Vertex.PACKAGEINFORMATION);
		this.name = name;
		this.manager = manager.toString();
		this.version = version;
		if (!url.equals("")) {
			this.repository = new Repo(type, url);
		}
	}

	public PackageInformation(String id, String name, PackageManager manager, String version) {
		super(id, Vertex.PACKAGEINFORMATION);
		this.name = name;
		this.manager = manager.toString();
		this.version = version;
	}

	public class Repo {

		private String type;

		private String url;

		Repo(String type, String url) {
			this.type = type;
			this.url = url;
		}
	}

	public String getName() {
		return this.name;
	}
}
